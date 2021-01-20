class Address {
  final String address;
  final String streetNumber;
  final String city;
  final String province;
  final String postalCode;
  final String country;

  Address(this.address, this.streetNumber, this.city, this.province,
      this.postalCode, this.country);

  Address.fromJson(Map<String, dynamic> json)
      : address = json['address'],
        streetNumber = json['streetNumber'],
        city = json['city'],
        province = json['province'],
        postalCode = json['postalCode'],
        country = json['country'];
}
