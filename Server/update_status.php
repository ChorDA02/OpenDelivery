<?php
$response = array();

if (isset($_POST['id']) && isset($_POST['courier_id']) && isset($_POST['courier']) && isset($_POST['status'])) {

    $id = $_POST['id'];
    $courierid = $_POST['courier_id'];
    $courier = $_POST['courier'];
    $status = $_POST['status'];
    $db = mysqli_connect('', '', '', '');
    $result = mysqli_query($db, "UPDATE `orders` SET `status` = '$status', `courier_id` = '$courierid', `courier` = '$courier', `coords_courier` = '0,0' WHERE `id` = '$id'");

    if ($result) {
        $response["success"] = 1;
        $response["message"] = "Status updated.";
	echo json_encode($response);
    } else {
        $response["success"] = 0;
        $response["message"] = "Error";

        echo json_encode($response);
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";

    echo json_encode($response);
}
?>
