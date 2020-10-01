def duration = Integer.parseInt(params.DURATION)
def threadList = params.THREADS.split(',')
def delayList = params.RAMPUP.split(',')
def interval = Integer.parseInt(params.INTERVAL)
def testcaseList = params.TESTCASE.split(',')
def rndResCnt = Integer.parseInt(params.RND_RES_CNT)
def cnvId = params.CNV_ID
int stageCount = 0;

if(cnvId==""){
    cnvId = "LT" + (new Date()).format("yyyyMMddHHmmss");
}


def runProject(stage_name, tc, duration, cnvId, threadCount, delay, rndResCnt, stageCount){
//    def title = email_prefix +" "+ profile + " " + duration.toString() + "D " + (new Date()).format("yyyy-MM-dd HH:mm:ss") + " #${BUILD_NUMBER}"
    def title =  "${BUILD_NUMBER}##"
    def timeOut = duration + 5;
    duration = (duration*60).toString()
    def _cnvId = cnvId + "-" + stageCount;
    def jmeter_home = "/home/jenkins/jmeter"
    stage stage_name + (threadCount==""?"":"-T"+threadCount.toString()) + (delay==""?"":"-D"+delay.toString())
    node {
        try{
            timeout(time: timeOut, unit: 'MINUTES') {
                sh "${jmeter_home}/bin/jmeter.sh -n -l ${jmeter_home}/prj/summary-report.csv -t ${jmeter_home}/prj/FCTG-LT-PP.jmx -JRND_RES_CNT=${rndResCnt} -JCNV_ID=${_cnvId} -JTESTCASE=${tc} -JTHREADS=${threadCount} -JRAMPUP=${delay} -JDURATION=${duration} -JLOOP_COUNT=1 -JSTARTUP_DELAY=0 -j ${jmeter_home}/prj/jmeter.log"

                readFile("${jmeter_home}/prj/summary-report.csv").split('\n').each { line, count -> echo line }
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
            runProject(tc, tc, duration, cnvId, threadCount, delay, rndResCnt, stageCount)
        }
    }
    sleep interval
}