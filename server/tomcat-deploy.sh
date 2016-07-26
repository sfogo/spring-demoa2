#!/usr/bin/env bash
#/bin/bash
TOMCAT_HOME="/usr/local/apache-tomcat-8.0.30"
APP="demoa2-1.0"
name="demoa2"
dir=`pwd`

# =========================
# Change Context Path
# =========================
changeContextPath() {
    tmpPage="/tmp/tmp.jsp"
    cp $1 ${tmpPage}
    cat ${tmpPage} | sed 's/"\/css/"\/demoa2\/css/' \
                   | sed 's/"\/js/"\/demoa2\/js/' \
                   | sed 's/"\/home/"\/demoa2\/home/' \
                   | sed 's/"\/images/"\/demoa2\/images/' \
                   | sed 's/"\/app/"\/demoa2\/app/' \
                   | sed 's/"\/login/"\/demoa2\/login/' \
                   | sed 's/"\/logout/"\/demoa2\/logout/' \
                   | sed 's/"\/oauth/"\/demoa2\/oauth/' > $1
    echo "-> $1"
}

# =========================
# Start
# =========================
echo "Deploy ${APP} to ${TOMCAT_HOME}/webapps/${name}"

rm -fR ${TOMCAT_HOME}/webapps/${name}
echo "Deleted ${TOMCAT_HOME}/webapps/${name}"

cp -R target/${APP} ${TOMCAT_HOME}/webapps
cd ${TOMCAT_HOME}/webapps
mv ${APP} ${name}
echo "Created ${TOMCAT_HOME}/webapps/${name}"

# No longer needed since contextPath is dynamically set
# for jsp in `ls ${name}/WEB-INF/jsp/*.jsp ${name}/WEB-INF/jsp/fragments/*.jsp` ; do
#    changeContextPath ${jsp}
# done

echo "Change Context Path for the following files:"
for h in `ls ${name}/WEB-INF/pages/admin/*.html` ; do
   changeContextPath ${h}
done

if [ "$1" == "-debug" ] ; then
    tmpCSS='/tmp/style.css'
    theCSS="${name}/WEB-INF/css/style.css"
    cp ${theCSS} ${tmpCSS}
    cat ${tmpCSS} | sed "s/hidden.*none;}/hidden {color:blue;}/" > ${theCSS}
fi

cd ${dir}
