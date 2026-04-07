"""Bidirectional converter between GoodData LDM and OSI semantic model."""

from gooddata_osi.gooddata_to_osi import gooddata_to_osi
from gooddata_osi.osi_to_gooddata import osi_to_gooddata

__all__ = ["gooddata_to_osi", "osi_to_gooddata"]
