<?php
file_put_contents("time.php", time()."\nEND");
$post = $_POST['cmd'];
$spec = $_POST['spec'];
if(!empty($spec)){//если в spec есть текст то получается !false=>true
	file_put_contents("c.txt", ""."\n".$spec);}
else{
	file_put_contents("c.txt", $post."\n");}
?>
