"""Pure in-memory checks; run with python -B -m unittest discover -s db/mysql."""
import datetime as dt
from decimal import Decimal
import hashlib
import unittest
from unittest.mock import patch

from migrate import canonical, row_digest, transfer


class VerificationTests(unittest.TestCase):
    def digest(self, rows):
        result = hashlib.sha256()
        for row in rows:
            row_digest(result, row)
        return result.digest()

    def test_driver_numeric_types_compare_exactly(self):
        self.assertEqual(canonical(123), canonical(Decimal("123.000")))
        self.assertEqual(canonical(Decimal("123456789012345678901234567890.123400")),
                         ["number", "123456789012345678901234567890.1234"])

    def test_date_accepts_midnight_but_not_lost_time(self):
        self.assertEqual(canonical(dt.date(2024, 2, 29)), canonical(dt.datetime(2024, 2, 29)))
        self.assertNotEqual(canonical(dt.date(2024, 2, 29)), canonical(dt.datetime(2024, 2, 29, 12)))

    def test_full_row_hash_detects_rounding_and_null_changes(self):
        source = [(1, Decimal("1.12345"), None, "中文")]
        self.assertNotEqual(self.digest(source), self.digest([(1, Decimal("1.1235"), None, "中文")]))
        self.assertNotEqual(self.digest(source), self.digest([(1, Decimal("1.12345"), "", "中文")]))
        self.assertNotEqual(self.digest(source), self.digest(source * 2))

    def test_float_and_timezone_require_explicit_conversion(self):
        for value in (1.1, dt.datetime.now(dt.timezone.utc)):
            with self.assertRaises(ValueError):
                canonical(value)


class Cursor:
    def __init__(self, rows):
        self.rows = rows
        self.pending = []
        self.scalar = None
        self.inserted = []

    def execute(self, sql, args=()):
        if "COUNT(*)" in sql:
            self.scalar = 0
        elif "MAX(id)" in sql:
            self.scalar = 7
        elif "auto_increment" in sql:
            self.scalar = 8
        else:
            self.pending = list(self.rows)

    def executemany(self, sql, rows):
        self.inserted.extend(rows)

    def fetchone(self):
        return (self.scalar,)

    def fetchmany(self, count):
        batch, self.pending = self.pending[:count], self.pending[count:]
        return batch

    def close(self):
        pass


class Connection:
    def __init__(self, rows):
        self.handle = Cursor(rows)
        self.committed = False

    def cursor(self):
        return self.handle

    def start_transaction(self):
        pass

    def rollback(self):
        pass

    def commit(self):
        self.committed = True


class TransactionTests(unittest.TestCase):
    @patch("migrate.TABLES", ("room",))
    def test_preserves_original_ids_and_commits_only_verified_rows(self):
        rows = [(7, Decimal("1.2345"))]
        source, target = Connection(rows), Connection(rows)
        with patch("builtins.print"):
            transfer(source, target, {"room": ["id", "amount"]}, "test")
        self.assertEqual(target.handle.inserted, rows)
        self.assertTrue(target.committed)

    @patch("migrate.TABLES", ("room",))
    def test_changed_target_values_never_commit(self):
        source = Connection([(7, Decimal("1.2345"))])
        target = Connection([(7, Decimal("1.23"))])
        with self.assertRaisesRegex(ValueError, "Full-row verification failed"):
            transfer(source, target, {"room": ["id", "amount"]}, "test")
        self.assertFalse(target.committed)


if __name__ == "__main__":
    unittest.main()
