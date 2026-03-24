"""Shared pytest fixtures for gooddata-osi convertor tests."""

from __future__ import annotations

import json
from pathlib import Path

import pytest
import yaml

from gooddata_osi.models import GdDeclarativeModel, gd_model_from_dict

FIXTURES_DIR = Path(__file__).parent / "fixtures"


@pytest.fixture()
def gooddata_tpcds_dict() -> dict:
    """Load the GoodData TPC-DS declarative model JSON fixture."""
    with open(FIXTURES_DIR / "gooddata_tpcds.json") as f:
        return json.load(f)


@pytest.fixture()
def gooddata_tpcds_model(gooddata_tpcds_dict: dict) -> GdDeclarativeModel:
    """Parse the GoodData TPC-DS fixture into typed dataclasses."""
    return gd_model_from_dict(gooddata_tpcds_dict)


@pytest.fixture()
def osi_tpcds_dict() -> dict:
    """Load the OSI TPC-DS YAML fixture."""
    with open(FIXTURES_DIR / "osi_tpcds.yaml") as f:
        return yaml.safe_load(f)
