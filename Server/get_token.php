<?php
$response = array();

if (isset($_POST['session_token'])) {

    $token = $_POST['session_token'];
    $db = mysqli_connect('', '', '', '');
    $result = mysqli_query($db, "SELECT * FROM users WHERE `session_token` = '$token'");

    if (!empty($result)) {
    	if(mysqli_num_rows($result) > 0) {
    		$result = mysqli_fetch_array($result);
    		$user = array();
		$user["id"] = $result["id"];
		$user["email"] = $result["email"];
    		$user["name"] = $result["name"];
    		$user["phone"] = $result["phone"];
		$user["courier"] = $result["courier"];
		$response["success"] = 1;
		$response["user"] = array();
		array_push($response["user"], $user);

		echo json_encode($response);
        } else {
        	$response["success"] = 0;
		$response["message"] = "Wrong token";

		echo json_encode($response);
        }
    } else {
        $response["success"] = 0;
        $response["message"] = "Wrong token";

        echo json_encode($response);
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";

    echo json_encode($response);
}
?>
