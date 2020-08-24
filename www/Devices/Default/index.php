<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<?php file_put_contents("time.php", time()."\nEND"); ?>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<pre>Last Online: <div id="lastonline"></div> </pre>
<pre>Status: <div id="status"></div> </pre>

<script src="https://code.jquery.com/jquery-3.1.0.min.js" type="text/javascript"></script>
 
<script type="text/javascript">
 
$(function() {
 
    getStatus();
 
});

$(function() {
 
    getOnline();
 
});

$(function() {
 
    getLocal();
 
});

$(function() {
 
    imgDinamic();
 
});

 
function getStatus() {
 
    $('div#status').load('show_output.php');
    setTimeout("getStatus()",2000);
 
}

function getOnline() {
 
    $('div#lastonline').load('getlastonline.php');
    setTimeout("getOnline()",2000);
 
}

function getLocal() {
 
    $('div#localfiles').load('getlist.php');
    setTimeout("getLocal()",2000);
 
}

function imgDinamic(){
	$('div#cimg').load('gimg.php').hide(true);
	$('img#imgD').attr('src', $('div#cimg').text());
	setTimeout("imgDinamic()",2000);
}

function audioDinamic(){
	$('div#caudio').load('gaudio.php').hide(true);
	$('audio#sourceMp3').attr('src', $('div#caudio').text());
}
 
</script>
 

<form action="cmd.php" method="post">
Command: <input type="text" name="cmd" id="cmd"><br>
Spec command: <input type="text" name="spec" id="spec"><br>
permission: <input type="text" name="permission" id="permission"><br>
zone: <input type="text" name="zone" id="zone"><br>
volume: <input type="text" name="volume" id="volume"><br>
stream: <input type="text" name="stream" id="stream"><br>
effect: <input type="text" name="effect" id="effect"><br>
value: <input type="text" name="value" id="value"><br>
path: <input type="text" name="path" id="path"><br>
</form>


<script type = "text/javascript" language = "javascript">
	 $(document).ready(function() {
		
		$("#driver").click(function(event){

			let def = {
		          path:$("#path").val(),
		          permission:$("#permission").val(),
		          zone:$("#zone").val(),
		          volume:$("#volume").val(),
		          stream:$("#stream").val(),
		          effect:$("#effect").val(),
		          value:$("#value").val(),

		          toString() {
		            return `{path: "${this.path}", permission: "${this.permission}",zone: "${this.zone}",volume: "${this.volume}",stream: "${this.stream}",effect: "${this.effect}",value: "${this.value}"}`;
		          }
		    };
			
		   $.post( 
			  "cmd.php",
				{ cmd: $("#cmd").val(),spec: $("#spec").val() + " " + def},
			  function(data) {
				 $('#stage').html(data);
			  }
		   );
				
		});
		$("#savephoto").click(function(event){
			$.post( 
			  "Photo/savephoto.php",
			  { savephotob64: $("div#cimg").text()},
			  function(data) {
				 $('#stage').html(data);
			  }
		   );
		});
		$("#clearaudio").click(function(event){
			
		   $.post( 
			  "Audio/clearAudio.php",
			  function(data) {
				 $('#stage').html(data);
			  }
		   );
				
		});
		$("button#loadedaudio").click(function(){
			audioDinamic();
		});
			
	 });
</script>
</head>
	
<body>

  <input type = "button" id = "driver" value = "send" />

</body>
<pre>Файлы на сервере:</pre>
<pre><div id="localfiles"></div></pre>


<pre>Фото с камеры: <div id="CamID"></div> </pre>
<img id="imgD" width="350" height="350" src="/"></img>
<div id="cimg"></div>
<pre>
<div id="caudio"></div>
<button id="loadedaudio">Загрузить Аудио</button>
<div>
	<input type = "button" id = "clearaudio" value = "Отчистить файл аудио" />
</div> 

<audio id="sourceMp3" autoplay controls src="">
	Not Supported.
</audio>
</pre>

</html>





