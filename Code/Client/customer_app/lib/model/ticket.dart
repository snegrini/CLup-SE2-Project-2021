import 'package:customer_app/enum/pass_status.dart';
import 'package:customer_app/model/store.dart';
import 'package:flutter/material.dart';

class Ticket {
  final int id;
  final String passCode;
  final PassStatus passStatus;
  final int queueNumber;
  final DateTime date;
  final TimeOfDay arrivalTime;
  final DateTime issuedAt;
  final Store store;

  Ticket(this.id, this.passCode, this.passStatus, this.queueNumber, this.date,
      this.arrivalTime, this.issuedAt, this.store);

  Ticket.fromJson(Map<String, dynamic> json)
      : id = json['ticketId'],
        passCode = json['passCode'],
        passStatus = json['passStatus'].toString().toPassStatus(),
        queueNumber = json['queueNumber'],
        date = DateTime.fromMillisecondsSinceEpoch(json['date']),
        arrivalTime = TimeOfDay(
            hour: int.parse(json['arrivalTime'].split(":")[0]),
            minute: int.parse(json['arrivalTime'].split(":")[1])),
        issuedAt = DateTime.fromMillisecondsSinceEpoch(json['issuedAt']),
        store = Store.fromJson(json['store']);
}
