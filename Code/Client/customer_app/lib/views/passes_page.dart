import 'package:customer_app/model/ticket.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/token_manager.dart';
import 'package:customer_app/views/ticket_detail_page.dart';
import 'package:flutter/material.dart';

class PassesPage extends StatefulWidget {
  @override
  _PassesState createState() => _PassesState();
}

class _PassesState extends State<PassesPage> {
  List<Ticket> _list;
  bool _loaded = false;
  bool _gotError = false;
  String _text;

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
      onTap: () => _openTicket(ticket),
    );
  }

  @override
  void initState() {
    super.initState();

    setState(() {
      _loaded = false;
    });

    _fetchStoreList();
  }

  Future<void> _fetchStoreList() async {
    try {
      String token = TokenManager().token;

      var storeList = await ApiManager.ticketListRequest(token);

      setState(() {
        _loaded = true;
        _list = storeList.map((e) => Ticket.fromJson(e)).toList();
      });
    } catch (err) {
      setState(() {
        _gotError = true;
        _text = err;
        _loaded = true;
      });
    }
  }

  void _openTicket(Ticket ticket) {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => TicketDetailPage(ticket: ticket)),
    );
  }
}
