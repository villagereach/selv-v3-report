1.1.0 / wip
==================

New functionality:
* [SELV3-886](https://openlmis.atlassian.net/browse/SELV3-886): Aligned with core openlmis-report 1.6.0 (OpenLMIS 3.20); the SELV printouts and the requisition print are kept on top of it.
  * Added `POST /api/reports/generate`, used by the 3.20 services to render printouts
  * Exposed Prometheus metrics at `/actuator/prometheus` and the health check used by Consul
  * Printouts keep their current look on the new base image: text is laid out with Liberation Sans
    (same widths as the Helvetica the PDFs use), so labels and product names are no longer cut
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
