<?php
echo(file_get_contents("c.txt"));
echo("\nEND");
file_put_contents("c.txt", "END");
file_put_contents("lastonline.txt", gmdate("d-m-Y\ H:i:s", microtime(true)));
?>