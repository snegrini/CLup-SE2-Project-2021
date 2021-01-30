import 'package:customer_app/model/ticket.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/data_manager.dart';
import 'package:customer_app/views/home_page.dart';
import 'package:flutter/material.dart';

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
            : Center(
                child: Column(
                  children: [
                    Text(widget.ticket.passCode),
                    RaisedButton(
                      onPressed: _disabled ? null : _deleteTicket,
                      child: Text(
                        'Delete the ticket',
                        style: TextStyle(color: Colors.white),
                      ),
                      color: ClupColors.grapefruit,
                      disabledColor: ClupColors.disabledGrapefruit,
                    ),
                    if (_error) Text(_errorText)
                  ],
                ),
              ));
  }

  @override
  void initState() {
    super.initState();

    if (widget.ticket != null) {
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
