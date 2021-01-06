import 'package:flutter/material.dart';

class Power {
  final int key;
  final int value;

  Power(this.key, this.value);

  Map<String, dynamic> toJson() => {
        'key': key,
        'value': value,
      };
}
