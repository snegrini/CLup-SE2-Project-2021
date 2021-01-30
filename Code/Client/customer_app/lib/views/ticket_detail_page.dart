import 'package:customer_app/enum/pass_status.dart';
import 'package:customer_app/model/ticket.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/data_manager.dart';
import 'package:customer_app/views/home_page.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:qr_flutter/qr_flutter.dart';

/// Page that displays the details of ticket
class TicketDetailPage extends StatefulWidget {
  int ticketId = -1;
  Ticket ticket;

  TicketDetailPage(this.ticketId);

  TicketDetailPage.fromTicket(this.ticket);

  _TicketDetailState createState() => _TicketDetailState();
}

class _TicketDetailState extends State<TicketDetailPage> {
  bool _loading = true;
  bool _disabled = false;
  bool _error = false;
  String _errorText = "";

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text('Ticket'),
          backgroundColor: ClupColors.grapefruit,
        ),
        body: _loading
            ? Center(
                child: CircularProgressIndicator(
                    valueColor: new AlwaysStoppedAnimation<Color>(
                        ClupColors.grapefruit)))
            : Container(
                margin: const EdgeInsets.symmetric(vertical: 7, horizontal: 10),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      widget.ticket.store.name,
                      style:
                          TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
                    ),
                    Text(widget.ticket.store.address.toString(),
                        style: TextStyle(color: Colors.grey, fontSize: 20)),
                    SizedBox(height: 18),
                    Center(
                      child: Column(
                        children: [
                          QrImage(
                            data: widget.ticket.passCode,
                            version: QrVersions.auto,
                            size: 200.0,
                          ),
                          SizedBox(height: 10),
                          Text(
                            'Date',
                            style: TextStyle(
                                fontSize: 20, fontWeight: FontWeight.bold),
                          ),
                          SizedBox(height: 5),
                          Text(
                              DateFormat('dd/MM/yyyy')
                                  .format(widget.ticket.date),
                              style: TextStyle(fontSize: 15)),
                          SizedBox(height: 10),
                          Text('Estimated Call Time',
                              style: TextStyle(
                                  fontSize: 20, fontWeight: FontWeight.bold)),
                          SizedBox(height: 5),
                          Text(widget.ticket.arrivalTime.format(context),
                              style: TextStyle(fontSize: 15)),
                          SizedBox(height: 10),
                          Text(
                            'Queue Number',
                            style: TextStyle(
                                fontSize: 20, fontWeight: FontWeight.bold),
                          ),
                          SizedBox(height: 5),
                          Text(widget.ticket.queueNumber.toString(),
                              style: TextStyle(fontSize: 15)),
                          SizedBox(height: 10),
                          Text(
                            'Ticket Status',
                            style: TextStyle(
                                fontSize: 20, fontWeight: FontWeight.bold),
                          ),
                          SizedBox(height: 5),
                          Text(widget.ticket.passStatus.name,
                              style: TextStyle(fontSize: 15)),
                          SizedBox(height: 30),
                          RaisedButton(
                            onPressed: _disabled ? null : _deleteTicket,
                            child: Text(
                              'Delete the ticket',
                              style: TextStyle(color: Colors.white),
                            ),
                            color: ClupColors.grapefruit,
                            disabledColor: ClupColors.disabledGrapefruit,
                          ),
                          SizedBox(height: 20),
                          if (_error) Text(_errorText)
                        ],
                      ),
                    )
                  ],
                )));
  }

  @override
  void initState() {
    super.initState();

    if (widget.ticket != null) {
      widget.ticketId = widget.ticket.id;

      setState(() {
        _loading = false;
      });
    } else {
      _ticketDetailRequest();
    }
  }

  Future<void> _ticketDetailRequest() async {
    try {
      var ticketJson = await ApiManager.ticketDetailRequest(
          DataManager().token, widget.ticketId);

      widget.ticket = Ticket.fromJson(ticketJson);
      setState(() {
        _loading = false;
        _error = false;
      });
    } catch (e) {
      setState(() {
        _loading = false;
        _error = true;
        _errorText = e;
      });
    }
  }

  Future<void> _deleteTicket() async {
    setState(() {
      _disabled = true;
    });

    try {
      await ApiManager.deleteTicketRequest(
          DataManager().token, widget.ticketId);

      Navigator.of(context).pop();
      Navigator.push(
        context,
        MaterialPageRoute(builder: (context) => HomePage()),
      );
    } catch (e) {
      setState(() {
        _error = true;
        _disabled = false;
        _errorText = e;
      });
    }
  }
}
