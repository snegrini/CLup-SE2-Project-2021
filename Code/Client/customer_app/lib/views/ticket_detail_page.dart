import 'package:customer_app/model/ticket.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:flutter/material.dart';
import 'package:qr_flutter/qr_flutter.dart';

/// Page that displays the details of ticket
class TicketDetailPage extends StatelessWidget {
  final Ticket ticket;

  TicketDetailPage({Key key, @required this.ticket}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text('Ticket'),
          backgroundColor: ClupColors.grapefruit,
        ),
        body: Center(
          child: new Column(
            children: [
              Text(ticket.store.name),
              Text(ticket.store.address.toString()),
              QrImage(
                data: ticket.passCode,
                version: QrVersions.auto,
                size: 225.0,
              ),
              Text(ticket.queueNumber.toString()),
              Text(ticket.date.toString()),
              Text(ticket.arrivalTime.toString())
            ],
          ),
        ));
  }

  void _deleteTicket() {

  }
}
