"""Round-trip conversion tests: GoodData → OSI → GoodData."""

from __future__ import annotations

from gooddata_osi.gooddata_to_osi import gooddata_to_osi
from gooddata_osi.models import GdDeclarativeModel
from gooddata_osi.osi_to_gooddata import osi_to_gooddata


def test_roundtrip_preserves_datasets(gooddata_tpcds_model: GdDeclarativeModel):
    """Verify GoodData → OSI → GoodData preserves dataset count and IDs."""
    # GoodData -> OSI
    osi = gooddata_to_osi(gooddata_tpcds_model, model_name="roundtrip_test")

    # OSI -> GoodData
    result = osi_to_gooddata(osi)

    original_ds_ids = {ds.id for ds in gooddata_tpcds_model.ldm.datasets}
    result_ds_ids = {ds.id for ds in result.ldm.datasets}

    # All original datasets should be present (date_dim goes to date_instances)
    non_date_original = original_ds_ids
    assert non_date_original == result_ds_ids


def test_roundtrip_preserves_date_instances(gooddata_tpcds_model: GdDeclarativeModel):
    """Verify date instances survive the round trip."""
    osi = gooddata_to_osi(gooddata_tpcds_model)
    result = osi_to_gooddata(osi)

    assert len(result.ldm.date_instances) == len(gooddata_tpcds_model.ldm.date_instances)

    original_di = gooddata_tpcds_model.ldm.date_instances[0]
    result_di = result.ldm.date_instances[0]
    assert result_di.id == original_di.id
    assert set(result_di.granularities) == set(original_di.granularities)


def test_roundtrip_preserves_references(gooddata_tpcds_model: GdDeclarativeModel):
    """Verify references/relationships survive the round trip."""
    osi = gooddata_to_osi(gooddata_tpcds_model)
    result = osi_to_gooddata(osi)

    original_ss = next(ds for ds in gooddata_tpcds_model.ldm.datasets if ds.id == "store_sales")
    result_ss = next(ds for ds in result.ldm.datasets if ds.id == "store_sales")

    original_targets = {ref.identifier.id for ref in original_ss.references}
    result_targets = {ref.identifier.id for ref in result_ss.references}
    assert original_targets == result_targets


def test_roundtrip_preserves_attribute_count(gooddata_tpcds_model: GdDeclarativeModel):
    """Verify attribute counts survive the round trip."""
    osi = gooddata_to_osi(gooddata_tpcds_model)
    result = osi_to_gooddata(osi)

    for orig_ds in gooddata_tpcds_model.ldm.datasets:
        result_ds = next((ds for ds in result.ldm.datasets if ds.id == orig_ds.id), None)
        assert result_ds is not None, f"Dataset {orig_ds.id} missing after roundtrip"
        assert len(result_ds.attributes) == len(orig_ds.attributes), (
            f"Attribute count mismatch for {orig_ds.id}: {len(result_ds.attributes)} != {len(orig_ds.attributes)}"
        )


def test_roundtrip_preserves_fact_count(gooddata_tpcds_model: GdDeclarativeModel):
    """Verify fact counts survive the round trip."""
    osi = gooddata_to_osi(gooddata_tpcds_model)
    result = osi_to_gooddata(osi)

    for orig_ds in gooddata_tpcds_model.ldm.datasets:
        result_ds = next((ds for ds in result.ldm.datasets if ds.id == orig_ds.id), None)
        assert result_ds is not None
        assert len(result_ds.facts) == len(orig_ds.facts), (
            f"Fact count mismatch for {orig_ds.id}: {len(result_ds.facts)} != {len(orig_ds.facts)}"
        )
