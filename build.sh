#!/bin/sh

# Sync translations with Transifex, then build.
#
# Two source resources are consumed from the shared openlmis-report project:
# the backend "messages" resource and the JasperReports "report-translations"
# bundle. The shared /transifex/sync_transifex.sh helper hardcodes a single
# "messages" resource per project, so the tx client is driven directly here.
#
# Pushing is off by default on purpose: the source strings of both resources
# are owned by the core openlmis-report service and a push from here would
# overwrite them. Deployment-specific labels belong in the override bundle
# mounted under /config/reports/resourceBundles instead.

TX_PUSH=${TRANSIFEX_PUSH:-false}
TX_PULL=${TRANSIFEX_PULL:-true}

add_resource() {
  tx add --organization=openlmis --project=openlmis-report --type=UNICODEPROPERTIES \
    --resource="$1" --file-filter="$2" "$3"
}

if [ -n "$TX_TOKEN" ]; then
  rm -rf .tx
  tx init
  add_resource messages \
    'src/main/resources/messages_<lang>.properties' \
    src/main/resources/messages_en.properties
  add_resource report-translations \
    'src/main/resources/resourceBundles/report_translations_<lang>.properties' \
    src/main/resources/resourceBundles/report_translations.properties

  [ "$TX_PUSH" = true ] && { tx push -s || exit 1; }
  [ "$TX_PULL" = true ] && { tx pull -a -f || exit 1; }
else
  echo "TX_TOKEN not set - skipping Transifex sync, using the bundle committed in the repo" >&2
fi
# Run Gradle build
gradle clean build
