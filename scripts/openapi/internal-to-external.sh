cat docs/openapi/pn-apikey-manager-internal-v1.yaml \
    | grep -v "# NO EXTERNAL" \
    | sed -e '/# ONLY EXTERNAL/s/^#//' \
    > docs/openapi/pn-apikey-manager-external-web-v1.yaml