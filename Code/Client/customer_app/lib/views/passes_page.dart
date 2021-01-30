import 'package:customer_app/enum/pass_status.dart';
import 'package:customer_app/model/ticket.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/data_manager.dart';
import 'package:customer_app/views/ticket_detail_page.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

/// Page that displays the list of store passes bound with the customer.
class PassesPage extends StatefulWidget {
  @override
  _PassesState createState() => _PassesState();
}

class _PassesState extends State<PassesPage> {
  List<Ticket> _list;
  bool _loaded = false;
  bool _gotError = false;
  String _text;

  /// Displays a progress bar if the page is loading, a text if the fetch got an
  /// error and the list if everything gone fine. If the list is empty a message
  /// is shown.
  @override
  Widget build(BuildContext context) {
    if (!_gotError) {
      if (_loaded) {
        if (_list.isEmpty) {
          return Center(child: Text('No passes here!'));
        } else {
          return ListView.separated(
            itemCount: _list.length,
            itemBuilder: (context, index) {
              return _buildRow(_list[index]);
            },
            separatorBuilder: (context, index) => Divider(color: Colors.grey),
          );
        }
      } else {
        return CircularProgressIndicator(
            valueColor:
                new AlwaysStoppedAnimation<Color>(ClupColors.grapefruit));
      }
    } else {
      return Center(child: Text(_text));
    }
  }

  Widget _buildRow(Ticket ticket) {
    return new ListTile(
      title: new Text('Ticket'),
      subtitle: Text(ticket.store.name),
      trailing: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(DateFormat('dd/MM/yyyy').format(ticket.date)),
          SizedBox(height: 2),
          (ticket.passStatus != PassStatus.valid)
              ? Text(
                  ticket.passStatus.name,
                  style: TextStyle(color: ClupColors.grapefruit),
                )
              : Text(ticket.arrivalTime.format(context),
                  style: TextStyle(color: Colors.green))
        ],
      ),
      onTap: () => _openTicket(ticket),
    );
  }

  @override
  void initState() {
    super.initState();
    _fetchTicketsList();
  }

  /// Fetches the list of tickets from the server.
  Future<void> _fetchTicketsList() async {
    try {
      String token = DataManager().token;

      var ticketList = await ApiManager.ticketListRequest(token);

      setState(() {
        _loaded = true;
        _list = ticketList.map((e) => Ticket.fromJson(e)).toList();
      });
    } catch (err) {
      setState(() {
        _gotError = true;
        _text = err;
        _loaded = true;
      });
    }
  }

  /// Handles the tap on a ticket from the list. The [ticket] is the one tapped.
  void _openTicket(Ticket ticket) {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => TicketDetailPage(ticket.id)),
    );
  }
}
