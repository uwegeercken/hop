package org.apache.hop.core.svg;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class HopSvgGraphics2DTest {

  private static final String BASIC_SVG_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><svg fill-opacity=\"1\" color-rendering=\"auto\" color-interpolation=\"auto\" text-rendering=\"auto\" "
    + "stroke=\"black\" stroke-linecap=\"square\" stroke-miterlimit=\"10\" shape-rendering=\"auto\" stroke-opacity=\"1\" fill=\"black\" stroke-dasharray=\"none\" font-weight=\"normal\" "
    + "stroke-width=\"1\" font-family=\"'Dialog'\" font-style=\"normal\" stroke-linejoin=\"miter\" font-size=\"12px\" stroke-dashoffset=\"0\" image-rendering=\"auto\" xmlns=\"http://www.w3"
    + ".org/2000/svg\">\n"
    + "  <!--Generated by the Batik Graphics2D SVG Generator-->\n"
    + "  <defs id=\"genericDefs\"/>\n"
    + "  <g/>\n"
    + "</svg>\n";

  private static final String BASIC_CIRCLE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><svg fill-opacity=\"1\" color-rendering=\"auto\" color-interpolation=\"auto\" text-rendering=\"auto\" "
    + "stroke=\"black\" stroke-linecap=\"square\" stroke-miterlimit=\"10\" shape-rendering=\"auto\" stroke-opacity=\"1\" fill=\"black\" stroke-dasharray=\"none\" font-weight=\"normal\" "
    + "stroke-width=\"1\" font-family=\"'Dialog'\" font-style=\"normal\" stroke-linejoin=\"miter\" font-size=\"12px\" stroke-dashoffset=\"0\" image-rendering=\"auto\" xmlns=\"http://www.w3"
    + ".org/2000/svg\">\n"
    + "  <!--Generated by the Batik Graphics2D SVG Generator-->\n"
    + "  <defs id=\"genericDefs\"/>\n"
    + "  <g>\n"
    + "    <g>\n"
    + "      <circle fill=\"none\" r=\"12.5\" cx=\"62.5\" cy=\"62.5\"/>\n"
    + "    </g>\n"
    + "  </g>\n"
    + "</svg>\n";

  @Test
  public void testNewDocumentXml() throws Exception {
    HopSvgGraphics2D graphics2D = HopSvgGraphics2D.newDocument();
    assertEquals(BASIC_SVG_XML, graphics2D.toXml());
  }

  @Test
  public void testNewDocumentSimpleXml() throws Exception {
    HopSvgGraphics2D graphics2D = HopSvgGraphics2D.newDocument();

    graphics2D.drawOval( 50, 50, 25, 25 );

    assertEquals(BASIC_CIRCLE_XML, graphics2D.toXml());
  }
}