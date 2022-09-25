<?php
$response = array();

if (isset($_POST['id']) && isset($_POST['coords'])) {

    $id = $_POST['id'];
    $coords = $_POST['coords'];
    $db = mysqli_connect('', '', '', '');
    $result = mysqli_query($db, "UPDATE `orders` SET `coords_courier` = '$coords' WHERE `courier_id` = '$id' and status != 'delivered'");

    if ($result) {
        $response["success"] = 1;
        $response["message"] = "Coords updated.";
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
