import 'package:flutter/material.dart';

class ListPage extends StatefulWidget {

  @override
  _ListState createState() => _ListState();
}

class _ListState extends State<ListPage> {
  final List<String> list = ['piero', 'paolo', 'marco','piero', 'paolo', 'marco','piero', 'paolo', 'marco','piero', 'paolo', 'marco','piero', 'paolo', 'marco'];

  @override
  Widget build(BuildContext context) {
    return new ListView.separated(
      itemCount: list.length,
      itemBuilder: (context, index) {
        return _buildRow(list[index]);
      },
      separatorBuilder: (context, index) => Divider(color: Colors.grey),
    );
  }

  Widget _buildRow(String name) {
    return new ListTile(
      title: new Text(name),
      onTap: () {
        showDialog(
            context: context,
            builder: (context) => AlertDialog(
              content: Text(name),
            ));
      },
    );
  }
}
