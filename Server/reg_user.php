<?php
$response = array();

if (isset($_POST['email']) && isset($_POST['password']) && isset($_POST['name']) && isset($_POST['phone'])) {

    $email = $_POST['email'];
    $password = $_POST['password'];
    $name = $_POST['name'];
    $phone = $_POST['phone'];
    $db = mysqli_connect('', '', '', '');
    $token = md5($email + rand(0,255) + $name + rand(0,255));
    $test = mysqli_query($db, "SELECT * FROM users WHERE email = '$email'");
    if (mysqli_num_rows($test) == 0) $result = mysqli_query($db, "INSERT INTO users(email, password, name, phone, session_token) VALUES('$email', '$password', '$name', '$phone', '$token')");

    if ((mysqli_num_rows($test) == 0) && $result) {
        $response["success"] = 1;
        $response["message"] = "User successfully registered.";
	$response["id"] = mysqli_fetch_array(mysqli_query($db, "SELECT id FROM users WHERE email = '$email' and password = '$password' and name = '$name' and phone = '$phone' LIMIT 1"))["id"];
        $response["token"] = $token;
	echo json_encode($response);
    } else {
        $response["success"] = 0;
        $response["message"] = "User already exists";

        echo json_encode($response);
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";

    echo json_encode($response);
}
?>
