"""Oracle -> empty MySQL database. Default: metadata-only, no data writes.

Dependencies (install separately): python-oracledb, mysql-connector-python.
Credentials are environment variables; never printed or written to a report.
Requires a maintenance window and a reviewed schema before --apply.
"""
import argparse
import datetime as dt
from decimal import Decimal
import hashlib
import json
import os
import sys

TABLES = (
    "role", "employee", "users", "room_type", "room", "room_type_image",
    "booking_order", "check_in_record", "check_out_settlement", "attendance",
    "performance", "review", "review_image", "favorite", "income_record", "expense_record",
)


def required(name):
    value = os.environ.get(name)
    if not value:
        raise ValueError(f"Missing environment variable: {name}")
    return value


def canonical(value):
    """Stable, loss-detecting representation across the two JDBC/DBAPI type systems."""
    if hasattr(value, "read"):
        value = value.read()
    if value is None:
        return ["null"]
    if isinstance(value, (int, Decimal)):
        # Avoid Decimal.normalize(): ambient precision can round large values.
        number = format(Decimal(value), "f")
        if "." in number:
            number = number.rstrip("0").rstrip(".")
        return ["number", "0" if Decimal(value) == 0 else number]
    if isinstance(value, dt.datetime):
        if value.tzinfo is not None:
            raise ValueError("Timezone-aware source timestamps require an explicit mapping")
        return ["date", value.isoformat(timespec="microseconds")]
    if isinstance(value, dt.date):
        return ["date", dt.datetime.combine(value, dt.time()).isoformat(timespec="microseconds")]
    if isinstance(value, str):
        return ["text", value]
    if isinstance(value, bytes):
        return ["bytes", value.hex()]
    raise ValueError(f"Unsupported value type: {type(value).__name__}")


def row_digest(hasher, row):
    encoded = json.dumps([canonical(v) for v in row], ensure_ascii=False,
                         separators=(",", ":")).encode("utf-8")
    hasher.update(len(encoded).to_bytes(8, "big"))
    hasher.update(encoded)


def scalar(cursor, sql, args=None):
    cursor.execute(sql, args or ())
    return cursor.fetchone()[0]


def inspect(source, target, database):
    sc, tc = source.cursor(), target.cursor()
    sc.execute("SELECT table_name FROM user_tables ORDER BY table_name")
    tables = {r[0] for r in sc}
    expected = {t.upper() for t in TABLES}
    if tables != expected:
        raise ValueError("Oracle table inventory differs; review ALL tables before migration. "
                         f"Missing={sorted(expected - tables)}, extra={sorted(tables - expected)}")
    tc.execute("SELECT table_name, engine FROM information_schema.tables "
               "WHERE table_schema = %s AND table_type = 'BASE TABLE'", (database,))
    target_tables = dict(tc.fetchall())
    if set(target_tables) != set(TABLES) or any(e != "InnoDB" for e in target_tables.values()):
        raise ValueError("Target must contain exactly the 16 reviewed InnoDB tables")
    if scalar(tc, "SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema = %s", (database,)):
        raise ValueError("Target triggers must be reviewed and removed from the import path")
    columns = {}
    for table in TABLES:
        sc.execute("SELECT column_name, data_type, data_precision, data_scale, char_length, nullable "
                   "FROM user_tab_columns WHERE table_name = :1 ORDER BY column_id", (table.upper(),))
        source_columns = sc.fetchall()
        tc.execute("SELECT column_name, column_type, is_nullable, extra FROM information_schema.columns "
                   "WHERE table_schema = %s AND table_name = %s ORDER BY ordinal_position", (database, table))
        target_columns = tc.fetchall()
        source_names = [r[0].lower() for r in source_columns]
        if set(source_names) != {r[0] for r in target_columns} or "id" not in source_names:
            raise ValueError(f"Column mismatch for {table}; no columns may be silently dropped or added")
        if not any(r[0] == "id" and "auto_increment" in r[3] for r in target_columns):
            raise ValueError(f"Missing auto-increment id: {table}")
        if scalar(tc, f"SELECT COUNT(*) FROM `{table}`"):
            raise ValueError(f"Target table is not empty: {table}")
        # These are metadata only: no passwords, customer data or source rows.
        print(json.dumps({"table": table, "oracle_columns": source_columns,
                          "mysql_columns": target_columns}, default=str))
        columns[table] = source_names
    sc.close()
    tc.close()
    return columns


def transfer(source, target, columns, database):
    """All inserts and verification share one transaction. Never commit per table."""
    target.rollback()
    target.start_transaction()
    sc, tc = source.cursor(), target.cursor()
    results = []
    for table in TABLES:
        names = columns[table]
        oracle_cols = ", ".join('"' + n.upper() + '"' for n in names)
        mysql_cols = ", ".join('`' + n + '`' for n in names)
        if scalar(tc, f"SELECT COUNT(*) FROM `{table}`"):
            raise ValueError(f"Target changed since inspection: {table}")
        sc.execute(f'SELECT {oracle_cols} FROM "{table.upper()}" ORDER BY "ID"')
        source_hash, count = hashlib.sha256(), 0
        while True:
            batch = sc.fetchmany(500)
            if not batch:
                break
            batch = [tuple(v.read() if hasattr(v, "read") else v for v in row) for row in batch]
            for row in batch:
                row_digest(source_hash, row)
            tc.executemany(f"INSERT INTO `{table}` ({mysql_cols}) VALUES "
                           f"({', '.join(['%s'] * len(names))})", batch)
            count += len(batch)
        tc.execute(f"SELECT {mysql_cols} FROM `{table}` ORDER BY `id`")
        target_hash, target_count = hashlib.sha256(), 0
        while True:
            batch = tc.fetchmany(500)
            if not batch:
                break
            for row in batch:
                row_digest(target_hash, row)
            target_count += len(batch)
        if count != target_count or source_hash.digest() != target_hash.digest():
            raise ValueError(f"Full-row verification failed: {table}; rolling back ALL inserts")
        maximum = scalar(tc, f"SELECT COALESCE(MAX(id), 0) FROM `{table}`")
        next_id = scalar(tc, "SELECT auto_increment FROM information_schema.tables "
                         "WHERE table_schema = %s AND table_name = %s", (database, table))
        if next_id is None or next_id <= maximum:
            raise ValueError(f"Auto-increment verification failed: {table}")
        results.append({"table": table, "rows": count, "sha256": source_hash.hexdigest()})
    target.commit()
    print(json.dumps({"status": "COMMITTED", "verified": results}))
    sc.close()
    tc.close()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apply", action="store_true", help="Insert and commit verified rows")
    parser.add_argument("--schema-reviewed", action="store_true")
    parser.add_argument("--writers-stopped", action="store_true")
    args = parser.parse_args()
    if args.apply and not (args.schema_reviewed and args.writers_stopped):
        parser.error("--apply requires --schema-reviewed and --writers-stopped")
    import oracledb
    import mysql.connector
    oracledb.defaults.fetch_decimals = True
    source = target = None
    try:
        source = oracledb.connect(user=required("ORACLE_USERNAME"),
                                 password=required("ORACLE_PASSWORD"), dsn=required("ORACLE_DSN"))
        # One consistent Oracle snapshot; this connection never executes source DML/DDL.
        with source.cursor() as cursor:
            cursor.execute("SET TRANSACTION READ ONLY")
        database = required("MYSQL_DATABASE")
        target = mysql.connector.connect(host=required("MYSQL_HOST"),
                                         port=int(os.environ.get("MYSQL_PORT", "3306")),
                                         user=required("MYSQL_USERNAME"), password=required("MYSQL_PASSWORD"),
                                         database=database, charset="utf8mb4", autocommit=False)
        with target.cursor() as cursor:
            # Session-only settings. Explicit zero IDs must not become newly generated IDs.
            cursor.execute("SET SESSION sql_mode = CONCAT_WS(',', @@sql_mode, "
                           "'STRICT_ALL_TABLES', 'NO_AUTO_VALUE_ON_ZERO')")
            cursor.execute("SET SESSION time_zone = '+08:00'")
            cursor.execute("SET SESSION information_schema_stats_expiry = 0")
        columns = inspect(source, target, database)
        if args.apply:
            transfer(source, target, columns, database)
        else:
            print("INSPECTED: metadata only; no rows inserted. Review schema and constraints before --apply.")
    finally:
        if target is not None:
            target.rollback()
            target.close()
        if source is not None:
            source.rollback()
            source.close()


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        # Driver messages can contain row data and connection details. Do not echo them.
        if isinstance(error, ValueError):
            print(f"STOP: {error}", file=sys.stderr)
        else:
            print(f"STOP: {type(error).__name__}; details withheld to protect credentials and customer data. "
                  "If connection was lost during COMMIT, inspect target before retrying.", file=sys.stderr)
        sys.exit(1)
