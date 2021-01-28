import 'package:customer_app/model/ticket.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/data_manager.dart';
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
  Widget _body;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Ticket'),
        backgroundColor: ClupColors.grapefruit,
      ),
      body: _body,
    );
  }

  @override
  void initState() {
    super.initState();
    _setLoadingBar();

    if (widget.ticket != null) {
      _buildTicketDetails();
    } else {
      _ticketDetailRequest();
    }
  }

  Future<void> _ticketDetailRequest() async {
    try {
      var ticketJson = await ApiManager.ticketDetailRequest(
          DataManager().token, widget.ticketId);

      widget.ticket = Ticket.fromJson(ticketJson);
      _buildTicketDetails();
    } catch (e) {
      setState(() {
        _setErrorPage(e);
      });
    }
  }

  void _buildTicketDetails() {
    setState(() {
      _body = Center(child: Text(widget.ticket.passCode));
    });
  }

  /// Sets a page for displaying a loading bar.
  void _setLoadingBar() {
    setState(() {
      _body = Center(
          child: CircularProgressIndicator(
              valueColor:
              new AlwaysStoppedAnimation<Color>(ClupColors.grapefruit)));
    });
  }

  void _setErrorPage(String error) {
    setState(() {
      _body = Center(child: Text(error));
    });
  }
}
