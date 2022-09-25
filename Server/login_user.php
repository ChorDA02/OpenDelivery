<?php
$response = array();

if (isset($_POST['email']) && isset($_POST['password'])) {

    $email = $_POST['email'];
    $password = $_POST['password'];
    $db = mysqli_connect('', '', '', '');

    $result = mysqli_query($db, "SELECT * FROM users WHERE `email` = '$email' and `password` = '$password'");

    if (!empty($result)) {
    	if(mysqli_num_rows($result) > 0) {
    		$result = mysqli_fetch_array($result);
    		$user = array();
		$user["id"] = $result["id"];
		$user["email"] = $result["email"];
    		$user["name"] = $result["name"];
    		$user["phone"] = $result["phone"];
		$user["courier"] = $result["courier"];
		$user["token"] = md5($user["email"] + rand(0,255) + $user["name"] + rand(0,255) + $user["phone"]);
		$token = $user["token"];
		$id = $user["id"];
		mysqli_query($db, "UPDATE users SET `session_token` = '$token' WHERE `id` = '$id'");
		$response["success"] = 1;
		$response["user"] = array();
		array_push($response["user"], $user);

		echo json_encode($response);
        } else {
        	$response["success"] = 0;
		$response["message"] = "User not found";

		echo json_encode($response);
        }
    } else {
        $response["success"] = 0;
        $response["message"] = "User not found";

        echo json_encode($response);
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";

    echo json_encode($response);
}
?>
