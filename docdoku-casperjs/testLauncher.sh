#!/bin/sh
casperjs test   --pre=pre.js \
                --post=logout.js \
                --includes=conf.js \
                --fail-fast \
                --xunit=testsResults.xml \
                tests/product-management/partCreation.js \
                tests/product-management/showPartDetails.js \
                tests/product-management/productCreation.js \
                tests/product-management/productDeletion.js \
                tests/product-management/partDeletion.js \
                tests/document-management/folderCreation.js \
                tests/document-management/folderDeletion.js \
