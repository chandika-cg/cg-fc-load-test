
node {
    try{
        def url = params.ENV + "/CachePrepopulationService/CachePrepopulationService";
        
        
        def data = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://service.prepopulation.tbx.codegen.it\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:performAccomPredictivePriceParamSearch1>" +
                "         <locations>"+params.LOCATIONS+"</locations>" +
                "         <locationType>"+params.LOACTION_TYPE+"</locationType>" +
                "         <runningDates></runningDates>" +
                "         <dateGenerateFrequency>"+params.FREQUENCY+"</dateGenerateFrequency>" +
                "         <predictivePriceSSP>"+params.SSP+"</predictivePriceSSP>" +
                "      </ser:performAccomPredictivePriceParamSearch1>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

        echo url;
        echo data;

        echo sh(script: "curl --location --request POST '$url' --header 'Content-Type: text/xml;charset=UTF-8' --data '$data'", returnStdout: true).trim()


    } catch (error) {
        echo error
    }
}

