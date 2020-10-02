def duration = Integer.parseInt(params.DURATION)
def threadList = params.THREADS.split(',')
def delayList = params.RAMPUP.split(',')
def interval = Integer.parseInt(params.INTERVAL)
def testcaseList = params.TESTCASE.split(',')
def rndResCnt = Integer.parseInt(params.RND_RES_CNT)
def noResponse = params.NO_RESPONSE
def jmeter_home = params.JMETER_HOME
def jmx_file = params.JMX_FILE
def cnvId = params.CNV_ID
int stageCount = 0;

def pipelineId = (new Date()).format("yyyyMMddHHmmss") + (Math.abs(new Random().nextInt() % [100]) + 1).toString();

if(cnvId==""){
    cnvId = "LT" + pipelineId;
}

def description = "";
description += "\n+------------------------------------------------+";
description += "\n+  CNV_ID                  = ${cnvId}";
description += "\n+  DURATION                = ${duration} MIN";
description += "\n+  INTERVAL                = ${interval} S";
description += "\n+  RESULT SHOW PROBABILITY = 1/${rndResCnt}";
description += "\n+------------------------------------------------+";
echo description;

def runProject(stage_name, tc, duration, noResponse, cnvId, threadCount, delay, rndResCnt, stageCount, pipelineId, jmeter_home, jmx_file){
//    def title = email_prefix +" "+ profile + " " + duration.toString() + "D " + (new Date()).format("yyyy-MM-dd HH:mm:ss") + " #${BUILD_NUMBER}"
    def title =  "${BUILD_NUMBER}##";
    def timeOut = duration + 5;
    duration = (duration*60).toString();
    def _cnvId = cnvId + "-" + stageCount;
//    def jmeter_home = "/home/jenkins/jmeter";
    def executionId = pipelineId + stageCount.toString();
    stage stage_name + (threadCount==""?"":"-T"+threadCount.toString()) + (delay==""?"":"-D"+delay.toString())
    node {
        try{
            timeout(time: timeOut, unit: 'MINUTES') {
                def description = "";
                description += "\n+------------------------------------------------+";
                description += "\n+  CNV_ID                  = ${_cnvId}";
                description += "\n+  TESTCASE                = ${tc}";
                description += "\n+  THREAD COUNT            = ${threadCount}";
                description += "\n+  DELAY                   = ${delay} S";
                description += "\n+------------------------------------------------+";
                echo description;

//                sh "export JAVA_HOME=/usr/local/java/jdk1.8.0_31/bin/java"
                sh "whoami"
                sh "echo $PATH"
                sh "java -version"

                sh "mkdir -p reports"
                sh "mkdir -p reports/${executionId}"
                sh "${jmeter_home}/bin/jmeter.sh -n -l ${jmeter_home}/prj/summary-report-${executionId}.jtl -t ${jmeter_home}/prj/${jmx_file} -JRND_RES_CNT=${rndResCnt} -JCNV_ID=${_cnvId} -JTESTCASE=${tc} -JTHREADS=${threadCount} -JRAMPUP=${delay} -JDURATION=${duration} -JNO_RESPONSE=${noResponse} -JLOOP_COUNT=1 -JSTARTUP_DELAY=0 -j ${jmeter_home}/prj/jmeter.log"
//                sh "${jmeter_home}/bin/jmeter.sh -n -l ${jmeter_home}/prj/summary-report-${executionId}.csv -t ${jmeter_home}/prj/FCTG-LT-PP.jmx -JRND_RES_CNT=${rndResCnt} -JCNV_ID=${_cnvId} -JTESTCASE=${tc} -JTHREADS=${threadCount} -JRAMPUP=${delay} -JDURATION=${duration} -JNO_RESPONSE=${noResponse} -JLOOP_COUNT=1 -JSTARTUP_DELAY=0 -j ${jmeter_home}/prj/jmeter.log -e -o reports/${executionId}"

                sh "mkdir -p summary"
                sh "mv gen-summary-report.html summary/summary-${executionId}.html"
                archiveArtifacts artifacts: "summary/summary-${executionId}.html", excludes: 'reports/*.md'
//                publishHTML (target: [
//                        allowMissing: false,
//                        alwaysLinkToLastBuild: false,
//                        keepAll: true,
//                        reportDir: "reports/${executionId}",
//                        reportFiles: 'index.html',
//                        reportName: "Load Test Report",
//                        includes: "**/*"
//                ])
//                sh "${jmeter_home}/bin/jmeter.sh -n -l ${jmeter_home}/prj/summary-report.csv -t ${jmeter_home}/prj/FCTG-LT-PP.jmx -JRND_RES_CNT=${rndResCnt} -JCNV_ID=${_cnvId} -JTESTCASE=${tc} -JTHREADS=${threadCount} -JRAMPUP=${delay} -JDURATION=${duration} -JLOOP_COUNT=1 -JSTARTUP_DELAY=0 -j ${jmeter_home}/prj/jmeter.log"


//                readFile("${jmeter_home}/prj/summary-report-${executionId}.csv").split('\n').each { line, count -> echo line }
            }
        } catch (error) {

        }
    }
}

testcaseList.each {
    def tc = it;
    threadList.each {
        def threadCount = it;
        delayList.each {
            def delay = it;
            stageCount++;
            runProject(tc, tc, duration, noResponse, cnvId, threadCount, delay, rndResCnt, stageCount, pipelineId, jmeter_home, jmx_file)
        }
    }
    sleep interval
}