import 'package:customer_app/model/address.dart';
import 'package:customer_app/model/opening_hour.dart';

class Store {
  final int id;
  final String name;
  final Address address;
  final String pec;
  final String phone;
  final List<OpeningHour> openingHours;
  final String image;
  final int customerInQueue;

  Store(this.id, this.name, this.address, this.pec, this.phone,
      this.openingHours, this.image, this.customerInQueue);

  Store.fromJson(Map<String, dynamic> json)
      : id = json['storeId'],
        name = json['storeName'],
        address = Address.fromJson(json['address']),
        pec = json['pecEmail'],
        phone = json['phone'],
        openingHours = List<OpeningHour>.unmodifiable(
            json['openingHours'].map((e) => OpeningHour.fromJson(e)).toList()),
        image = json['imagePath'],
        customerInQueue = json['customersInQueue'];
}
