import 'package:flutter/material.dart';

class OpeningHour {
  final TimeOfDay from;
  final TimeOfDay to;
  final int weekDay;

  OpeningHour(this.from, this.to, this.weekDay);

  OpeningHour.fromJson(Map<String, dynamic> json)
      : from = TimeOfDay(
            hour: int.parse(json['fromTime'].split(":")[0]),
            minute: int.parse(json['fromTime'].split(":")[1])),
        to = TimeOfDay(
            hour: int.parse(json['toTime'].split(":")[0]),
            minute: int.parse(json['toTime'].split(":")[1])),
        weekDay = json['weekDay'];
}
