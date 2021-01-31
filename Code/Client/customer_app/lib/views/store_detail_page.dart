import 'dart:collection';
import 'dart:convert';

import 'package:customer_app/enum/day_of_week.dart';
import 'package:customer_app/model/opening_hour.dart';
import 'package:customer_app/model/store.dart';
import 'package:customer_app/model/ticket.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/data_manager.dart';
import 'package:customer_app/views/ticket_detail_page.dart';
import 'package:flutter/material.dart';

class StoreDetailPage extends StatefulWidget {
  final int storeId;

  const StoreDetailPage(this.storeId);

  _StoreDetailState createState() => _StoreDetailState();
}

class _StoreDetailState extends State<StoreDetailPage> {
  Store _store;
  bool _loading = true;
  bool _disabled = false;
  bool _error = false;
  String _errorText = "";
  Map<String, List<OpeningHour>> _ohMap;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text('Store'),
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
                        _store.name,
                        style: TextStyle(
                            fontSize: 23, fontWeight: FontWeight.bold),
                      ),
                      Text(_store.address.toString(),
                          style: TextStyle(color: Colors.grey, fontSize: 18)),
                      SizedBox(height: 4),
                      Center(
                          child: Column(
                        children: [
                          SizedBox(
                              height: 140.0,
                              width: 140.0,
                              child: Image.memory(base64Decode(_store.image))),
                          SizedBox(height: 4),
                          Row(
                            children: <Widget>[
                              Spacer(flex: 1),
                              Expanded(
                                flex: 6,
                                child: Text('Estimated wait time',
                                    style: TextStyle(
                                        fontSize: 16,
                                        fontWeight: FontWeight.bold)),
                              ),
                              Expanded(
                                flex: 3,
                                child: Text(
                                    _store.estimateTime.toString() + " min",
                                    style: TextStyle(fontSize: 16)),
                              ),
                            ],
                          ),
                          SizedBox(height: 5),
                          Row(
                            children: <Widget>[
                              Spacer(flex: 1),
                              Expanded(
                                flex: 6,
                                child: Text('People in queue',
                                    style: TextStyle(
                                        fontSize: 16,
                                        fontWeight: FontWeight.bold)),
                              ),
                              Expanded(
                                flex: 3,
                                child: Text(
                                    _store.customerInQueue.toString() +
                                        " persons",
                                    style: TextStyle(fontSize: 16)),
                              ),
                            ],
                          ),
                          SizedBox(height: 15),
                          Text("Opening hours",
                              style: TextStyle(
                                  fontSize: 18, fontWeight: FontWeight.bold)),
                          SizedBox(height: 10),
                          // List of opening hours
                          for (var ohEntry in _ohMap.entries)
                            Padding(
                                padding:
                                    const EdgeInsets.symmetric(vertical: 3.5),
                                child: Row(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: <Widget>[
                                      Spacer(flex: 1),
                                      Expanded(
                                        flex: 6,
                                        child: Text(ohEntry.key,
                                            style: TextStyle(
                                                fontWeight: FontWeight.bold)),
                                      ),
                                      Expanded(
                                        flex: 3,
                                        child: Column(children: [
                                          for (var oh in ohEntry.value)
                                            Row(children: <Widget>[
                                              Text(oh.from.format(context) +
                                                  " - " +
                                                  oh.to.format(context))
                                            ])
                                        ]),
                                      ),
                                    ])),
                          SizedBox(height: 20),
                          RaisedButton(
                            onPressed: _disabled ? null : _addTicket,
                            child: Text(
                              'Retrieve a ticket',
                              style: TextStyle(color: Colors.white),
                            ),
                            color: ClupColors.grapefruit,
                            disabledColor: ClupColors.disabledGrapefruit,
                          ),
                          SizedBox(height: 20),
                          if (_error) Text(_errorText)
                        ],
                      ))
                    ])));
  }

  @override
  void initState() {
    super.initState();
    _storeDetailRequest();
  }

  Future<void> _storeDetailRequest() async {
    try {
      var storeJson = await ApiManager.storeDetailRequest(
          DataManager().token, widget.storeId);

      setState(() {
        _loading = false;
        _error = false;
        _store = Store.fromJson(storeJson);
        _ohMap = _ohListToMap(_store.openingHours);
      });
    } catch (e) {
      setState(() {
        _loading = false;
        _error = true;
        _errorText = e;
      });
    }
  }

  Future<void> _addTicket() async {
    setState(() {
      _disabled = true;
    });

    try {
      var storeJson = await ApiManager.addTicketRequest(
          DataManager().token, widget.storeId);

      Ticket ticket = Ticket.fromJson(storeJson);
      setState(() {
        _error = false;
        _disabled = false;
      });

      _redirectToTicketDetail(ticket);
    } catch (e) {
      setState(() {
        _error = true;
        _disabled = false;
        _errorText = e;
      });
    }
  }

  void _redirectToTicketDetail(Ticket ticket) {
    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) => TicketDetailPage.fromTicket(ticket)),
    );
  }

  Map<String, List<OpeningHour>> _ohListToMap(List<OpeningHour> ohList) {
    Map<String, List<OpeningHour>> openingHourMap = new LinkedHashMap();

    for (OpeningHour oh in ohList) {
      String dayName = DayOfWeekHelper.of(oh.weekDay).name;

      openingHourMap.putIfAbsent(dayName, () => List());
      openingHourMap[dayName].add(oh);
    }
    return openingHourMap;
  }
}
