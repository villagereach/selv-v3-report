1.0.6 / wip
==================

New functionality:
* [SELV3-847](https://openlmis.atlassian.net/browse/SELV3-847): Added the EPI footer (number of volumes, number of ice packs, person responsible for packing, truck registration, trailer registration, security seal, exchange rate, total amount in USD and total amount in MZM) to the Proof of Delivery and Order reports.

1.0.5 / 2025-02-20
==================

Fixes and Improvements:
* [SELVSUP-6](https://openlmis.atlassian.net/browse/SELVSUP-6):
  * The report is now filtered based on the user's STOCK_CARDS_VIEW permission (Supervision roles). Users will only see data they have access to. 
  * Added district and facilityType columns. 
  * Improved text formatting for enhanced readability. 
  * Fixed a subquery issue — previously, it displayed data for a single facility; now, it provides a summarized view across all facilities. 
  * Included products without batch numbers in the report. 
  * Added XLSX support, allowing users to export data in Excel format.
