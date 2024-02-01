import java.awt.*;
import java.awt.geom.Path2D;
import java.math.BigInteger;

public class BigIntRectangle extends Path2D.Double {

    private BigInteger x;
    private BigInteger y;
    private BigInteger width;
    private BigInteger height;

    public BigIntRectangle(BigInteger x, BigInteger y, BigInteger width, BigInteger height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        createPath();
    }

    public void createPath() {
        // Move to starting point
        moveTo(x.doubleValue(), y.doubleValue());

        // Line to other points to create a rectangle
        lineTo(x.add(width).doubleValue(), y.doubleValue());
        lineTo(x.add(width).doubleValue(), y.add(height).doubleValue());
        lineTo(x.doubleValue(), y.add(height).doubleValue());

        // Close the path to create a rectangle
        closePath();
    }

}
