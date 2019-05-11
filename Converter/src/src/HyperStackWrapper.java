package src;
import loci.formats.in.ND2Reader;

public class HyperStackWrapper {
	int cChannels;
	int zSlices;
	int tSeries;


	public HyperStackWrapper(int sizeC, int sizeZ, int sizeT) {
		cChannels = sizeC;
		zSlices = sizeZ;
		tSeries = sizeT;
	}

	public HyperStackWrapper(ND2Reader reader) {
		this(reader.getSizeC(), reader.getSizeZ(), reader.getSeriesCount());
	}

	@Override
	public String toString() {
		return "Channels: " + cChannels + " Z-Slices " + zSlices + " Time-Series " + tSeries;
	}

	public HSD getHyperStack(int stack) {
		int i = 0;
		for (int t = 0; t < tSeries; t++) {
			for (int z = 0; z < zSlices; z++) {
				for (int c = 0; c < cChannels; c++) {

					if (i == stack) return new HSD(c, z, t);
					i++;
					//System.out.println("C: "+c+" Z: "+z+" T: "+t);

				}
			}
		}
		throw new IndexOutOfBoundsException("Index " + i + " is out of bounds for T Series: " + tSeries + ", Z Slices: " + zSlices + ", and C Channels: " + cChannels);

	}

	public int getStack(int channel, int slice, int series) {
		int i = 0;
		for (int t = 0; t <= series; t++)
			for (int z = 0; z <= slice; z++)
				for (int c = 0; c <= channel; c++)
					i++;
		return i;
	}
}

class HSD {
	int c;
	int z;
	int t;

	public HSD(int c, int z, int t) {
		this.c = c;
		this.z = z;
		this.t = t;
	}


	public boolean equals(HSD hsd) {
		return hsd.c == this.c && hsd.z == this.z && hsd.t == this.t;
	}

	@Override
	public String toString() {
		return "C: " + c + "Z: " + z + "T: " + t;
	}
}