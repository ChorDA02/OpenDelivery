<?php
$response = array();

if (isset($_POST['customer_id']) && isset($_POST['customer']) && isset($_POST['price']) && isset($_POST['address']) && isset($_POST['coords']) && isset($_POST['address2']) && isset($_POST['coords2'])) {

    $customerid = $_POST['customer_id'];
    $customer = $_POST['customer'];
    $price = $_POST['price'];
    $address = $_POST['address'];
    $coords = $_POST['coords'];
    $address2 = $_POST['address2'];
    $coords2 = $_POST['coords2'];
    $db = mysqli_connect('', '', '', '');
    $result = mysqli_query($db, "INSERT INTO orders(customer_id, customer, price, address, coords, address2, coords2) VALUES('$customerid', '$customer', '$price', '$address', '$coords', '$address2', '$coords2')");

    if ($result) {
        $response["success"] = 1;
        $response["message"] = "Order created successfully.";
	$response["id"] = mysqli_fetch_array(mysqli_query($db, "SELECT id FROM orders WHERE customer_id = '$customerid' and customer = '$customer' and price = '$price' and address = '$address' and address2 = '$address2' LIMIT 1"))["id"];
	echo json_encode($response);
    } else {
        $response["success"] = 0;
        $response["message"] = "Order already exists";

        echo json_encode($response);
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";

    echo json_encode($response);
}
?>
