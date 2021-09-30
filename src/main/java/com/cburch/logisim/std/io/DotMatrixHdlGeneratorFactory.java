/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.io;

import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.fpga.designrulecheck.Netlist;
import com.cburch.logisim.fpga.designrulecheck.netlistComponent;
import com.cburch.logisim.fpga.hdlgenerator.Hdl;
import com.cburch.logisim.fpga.hdlgenerator.InlinedHdlGeneratorFactory;
import com.cburch.logisim.util.LineBuffer;

public class DotMatrixHdlGeneratorFactory extends InlinedHdlGeneratorFactory {

  @Override
  public LineBuffer getInlinedCode(Netlist netlist, Long componentId, netlistComponent componentInfo, String circuitName) {
    final var contents = LineBuffer.getHdlBuffer();
    final var colBased = componentInfo.getComponent().getAttributeSet().getValue(DotMatrixBase.ATTR_INPUT_TYPE) == DotMatrixBase.INPUT_COLUMN;
    final var rowBased = componentInfo.getComponent().getAttributeSet().getValue(DotMatrixBase.ATTR_INPUT_TYPE) == DotMatrixBase.INPUT_ROW;
    final var rows = componentInfo.getComponent().getAttributeSet().getValue(DotMatrix.ATTR_MATRIX_ROWS).getWidth();
    final var cols = componentInfo.getComponent().getAttributeSet().getValue(DotMatrix.ATTR_MATRIX_COLS).getWidth();

    if (colBased) {
      /* The simulator uses here following addressing scheme (2x2):
       *  r1,c0 r1,c1
       *  r0,c0 r0,c1
       *
       *  hence the rows are inverted to the definition of the LED-Matrix that uses:
       *  r0,c0 r0,c1
       *  r1,c0 r1,c1
       */
      for (var dotMatrixRow = 0; dotMatrixRow < rows; dotMatrixRow++) {
        final var ledMatrixRow = rows - dotMatrixRow - 1;
        for (var ledMatrixCol = 0; ledMatrixCol < cols; ledMatrixCol++) {
          final var wire = (rows == 1) ? Hdl.getNetName(componentInfo, ledMatrixCol, true, netlist)
              : Hdl.getBusEntryName(componentInfo, ledMatrixCol, true, dotMatrixRow, netlist);
          final var idx = (ledMatrixRow * cols) + ledMatrixCol + componentInfo.getLocalBubbleOutputStartId();
          contents.add("{{assign}} {{1}}{{<}}{{2}}{{>}} {{=}} {{3}};", LOCAL_OUTPUT_BUBBLE_BUS_NAME,
              idx, wire);
        }
      }
    } else if (rowBased) {
      /* The simulator uses here following addressing scheme (2x2):
       *  r1,c1 r1,c0
       *  r0,c1 r0,c0
       *
       *  hence the cols are inverted to the definition of the LED-Matrix that uses:
       *  r0,c0 r0,c1
       *  r1,c0 r1,c1
       */
      for (var ledMatrixRow = 0; ledMatrixRow < rows; ledMatrixRow++) {
        for (var dotMatrixCol = 0; dotMatrixCol < cols; dotMatrixCol++) {
          final var ledMatrixCol = cols - dotMatrixCol - 1;
          final var wire = (cols == 1) ? Hdl.getNetName(componentInfo, ledMatrixRow, true, netlist)
              : Hdl.getBusEntryName(componentInfo, ledMatrixRow, true, ledMatrixCol, netlist);
          final var idx = (ledMatrixRow * cols) + dotMatrixCol + componentInfo.getLocalBubbleOutputStartId();
          contents.add("{{assign}} {{1}}{{<}}{{2}}{{>}} {{=}} {{3}};", LOCAL_OUTPUT_BUBBLE_BUS_NAME,
              idx, wire);
        }
      }
    } else {
      /* The simulator uses here following addressing scheme (2x2):
       *  r1,c0 r1,c1
       *  r0,c0 r0,c1
       *
       *  hence the rows are inverted to the definition of the LED-Matrix that uses:
       *  r0,c0 r0,c1
       *  r1,c0 r1,c1
       */
      for (var dotMatrixRow = 0; dotMatrixRow < rows; dotMatrixRow++) {
        final var ledMatrixRow = rows - dotMatrixRow - 1;
        for (var ledMatrixCol = 0; ledMatrixCol < cols; ledMatrixCol++) {
          final var rowWire = (rows == 1) ? Hdl.getNetName(componentInfo, 1, true, netlist)
              : Hdl.getBusEntryName(componentInfo, 1, true, dotMatrixRow, netlist);
          final var colWire = (cols == 1) ? Hdl.getNetName(componentInfo, 0, true, netlist)
              : Hdl.getBusEntryName(componentInfo, 0, true, ledMatrixCol, netlist);
          final var idx = (ledMatrixRow * cols) + ledMatrixCol + componentInfo.getLocalBubbleOutputStartId();
          contents.add("{{assign}} {{1}}{{<}}{{2}}{{>}} {{=}} {{3}} {{and}} {{4}};", LOCAL_OUTPUT_BUBBLE_BUS_NAME,
              idx, rowWire, colWire);
        }
      }
    }
    contents.add("");

    return contents;
  }

  @Override
  public boolean isHdlSupportedTarget(AttributeSet attrs) {
    return attrs.getValue(DotMatrixBase.ATTR_PERSIST) == 0;
  }
}
