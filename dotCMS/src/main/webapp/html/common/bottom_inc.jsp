<iframe name="hidden_iframe" id="hidden_iframe" style="position:absolute;top:-100px;width:0px; height:0px; border: 0px;"></iframe>
<script>
	function setKeepAlive(){
		var myId=document.getElementById("hidden_iframe");
		myId.src ="/html/common/keep_alive.jsp?r=<%=System.currentTimeMillis()%>";
	}
	function killSession(){
		window.location = "/c/portal/logout?referer=/c";
	}
	<% if(Config.getStringProperty("KEEP_SESSION_ALIVE").equalsIgnoreCase("true")) {%>
		// 15 minutes
		setTimeout("setKeepAlive()", 60000 * 15);
	<%}else{%>
		// 30 minutes
		setTimeout("killSession()", 60000 * 30);
	<%} %>

		function dotMakeBodVisible(){

			if(!window.frameElement){
				console.log("bottom_inc.jsp frame busting");
				window.top.location="/dotAdmin/";
				return;
			}
			
			if(dojo.style(dojo.body(), "visibility") != "visible"){
				setTimeout( "dotMakeBodVisible()",3000);
				dojo.style(dojo.body(), "visibility", "visible");
				
			}

		}
		
		dojo.addOnLoad(dotMakeBodVisible);
		

	</script>
	
	<%@ include file="/html/common/db_enterprise_warning.jsp" %>
</body>
</html>
