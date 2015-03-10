package general;

/**
 * 
 * @author felix
 *
 *
 *         memory is simple class for saving all the different 2Dfloat-Arrays
 *         and providing access to them in the different classes
 */
public class Memory {

	private float[][] h;
	private float[][] hu;
	private float[][] hv;
	private float[][] b;

	private final int col, row;
	private float dX=0.5f, dY=0.5f;

	/**
	 * Initializes all Arrays with the given number of columns (x) and rows (y)
	 * 
	 * @param col
	 *            number of columns
	 * @param row
	 *            number of rows
	 */
	public Memory(int col, int row) {
		System.out.println("Memory with #col " + col + " and #row " + row);
		this.col = col;
		this.row = row;
		h = new float[col][row];
		hu = new float[col][row];
		hv = new float[col][row];
		b = new float[col][row];
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}
	
	public float[][] getMem(char mode) {
		switch (mode)
		{
			case 'h': return getH();
			case 'b': return getB();
			case 'u': return getHu();
			case 'v': return getHv();
			case 'w': // w for water height in nc file bathymetry 
				float[][] negativeH = new float[col-2][row-2];//return getH();
				for (int i=0; i<col-2; i++)
					for (int j=0; j<row-2; j++)
						negativeH[i][j] = (-1) * h[i+1][j+1] * 20;
				// to get an output compatible to SWE the factor 20 is used
				// because the reader won't be able to read those files
				// 20 is enough because 1 is the minimum positive value
				// that WaveSim can draw
				return negativeH;
			case 'd': // d for "displacement" in nc file displacement
				float[][] displ = new float[col-2][row-2];//return getH();
				for (int i=0; i<col-2; i++)
					for (int j=0; j<row-2; j++)
						displ[i][j] = (h[i+1][j+1] + b[i+1][j+1]) * 20;
				// to get an output compatible to SWE the factor 20 is used
				// because the reader won't be able to read those files
				return displ;
			default:  break;
		}
		return null;
	}

	public float[][] getH() {
		return h;
	}

	public float[][] getHu() {
		return hu;
	}

	public float[][] getHv() {
		return hv;
	}

	public float[][] getB() {
		return b;
	}

	public void setHu(float[][] hu) {
		if (hu.length == col && hu[0].length == row)
			this.hu = hu;
		else
			throw new IllegalArgumentException("Col and Row must be equal"
					+ " to getRow and getCol");
	}

	public void setHv(float[][] hv) {
		if (hv.length == col && hv[0].length == row)
			this.hv = hv;
		else
			throw new IllegalArgumentException("Col and Row must be equal"
					+ " to getRow and getCol");
	}

	public void setB(float[][] b) {
		if (b.length == col && b[0].length == row)
			this.b = b;
		else
			throw new IllegalArgumentException("Col and Row must be equal"
					+ " to getRow and getCol");
	}

	public void setH(float[][] h) {
		if (h.length == col && h[0].length == row)
			this.h = h;
		else
			throw new IllegalArgumentException("Col and Row must be equal"
					+ " to getRow and getCol");
	}

	public float getDX() {
		return dX;
	}

	public float getDY() {
		return dY;
	}

	public void setDxDy(float dx2, float dy2) {
		dX=dx2;
		dY=dy2;
	}

}
