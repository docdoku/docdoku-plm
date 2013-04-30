#!/bin/sh
casperjs test   --pre=login.js \
                --post=logout.js \
                --includes=conf.js \
                --fail-fast \
                tests/product-management/partCreation.js \
                tests/product-management/showPartDetails.js \
                tests/product-management/productCreation.js \
                tests/product-management/productDeletion.js \
                tests/product-management/partDeletion.js \
