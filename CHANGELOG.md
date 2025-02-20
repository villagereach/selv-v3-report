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
