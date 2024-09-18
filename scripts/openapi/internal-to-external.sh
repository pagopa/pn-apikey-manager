cat docs/openapi/pn-apikey-manager-internal.yaml \
    | grep -v "# NO EXTERNAL" \
    | sed -e '/# ONLY EXTERNAL/s/^#//' \
    > docs/openapi/pn-apikey-manager-external-web.yaml

cat docs/openapi/pn-apikey-manager-pg-internal.yaml \
    | grep -v "# NO EXTERNAL" \
    | sed -e '/# ONLY EXTERNAL/s/^#//' \
    > docs/openapi/pn-apikey-manager-pg-external-web.yaml