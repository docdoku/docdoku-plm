#!/bin/sh
domain="localhost";
port="8080";
login="test";
pass="test";
workspace="test";

while :
do
    case "$1" in
        -h | --host)
            domain=$2
            shift 2
            ;;
        -P | --port)
            port=$2
            shift 2
            ;;
        -u | --user)
            login=$2
            shift 2
            ;;
        -p | --password)
            pass=$2
            shift 2
            ;;
        -w | --workspace)
            workspace=$2
            shift 2
            ;;
        --) shift
            break
            ;;
        -*) echo "Erro: Unknown option : $1" >&2
            exit 1
            ;;
        *) break
            ;;
    esac
done

casperjs test   --pre=pre.js \
                --post=logout.js \
                --includes=conf.js,Tools.js \
                --fail-fast \
                --xunit=testsResults.xml \
                --domain=$domain \
                --port=$port \
                --login=$login \
                --pass=$pass \
                --workspace=$workspace \
                --verbose \
                --log-level=error \
                tests/product-management/part/partCreation.js \
                tests/product-management/part/showPartDetails.js \
                tests/product-management/product/productCreation.js \
                tests/product-management/product/productDeletion.js \
                tests/product-management/part/partDeletion.js \
                tests/document-management/folder/folderCreation.js \
                tests/document-management/folder/folderDeletion.js