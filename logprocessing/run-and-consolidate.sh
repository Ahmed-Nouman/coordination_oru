#!/bin/bash

set -eu

[ $# = 0 ]

root=$(dirname "$0")

timeout=60m
demo=GridTest
scenarios=(
  BASELINE_IDEAL_DRIVER_AUTOMATED_FIRST
  BASELINE_IDEAL_DRIVER_HUMAN_FIRST
  BASELINE_IDEAL_DRIVER_FIRST_COME
  FORCING_CS1_PRIORITIES_CHANGE
#  FORCING_CS1_WITH_STOPS
#  FORCING_CS1_CS2_PRIORITIES_CHANGE
#  FORCING_CS1_CS2_WITH_STOPS
#  FORCING_UPCOMING_PRIORITIES_CHANGE
#  FORCING_UPCOMING_WITH_STOPS
#  FORCING_GLOBAL_STOP
#  FORCING_GLOBAL_STOP_11
#  FORCING_GLOBAL_STOP_12
#  FORCING_UPCOMING_PRIORITIES_CHANGE_22
#  FORCING_UPCOMING_PRIORITIES_CHANGE_21
)

reference=$(mktemp --tmpdir run-and-consolidate.XXXX)
trap 'rm -f "$reference"' EXIT

set -x
"$root"/run-scenarios.sh "$timeout" "$demo" "${scenarios[@]}"
"$root"/consolidate-rundirs-to-sorted-csv.sh "$reference"