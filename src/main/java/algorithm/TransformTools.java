package algorithm;

import mpicbg.imglib.util.Util;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewTransform;
import mpicbg.spim.data.registration.ViewTransformAffine;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AbstractTranslation;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.Translation2D;
import net.imglib2.realtransform.Translation3D;

public class TransformTools {
	
	public static String printRealInterval( final RealInterval interval )
	{
		String out = "(Interval empty)";

		if ( interval == null || interval.numDimensions() == 0 )
			return out;

		out = "[" + interval.realMin( 0 );

		for ( int i = 1; i < interval.numDimensions(); i++ )
			out += ", " + interval.realMin( i );

		out += "] -> [" + interval.realMax( 0 );

		for ( int i = 1; i < interval.numDimensions(); i++ )
			out += ", " + interval.realMax( i );

		out += "]";

		return out;
	}

	public static AffineTransform3D createTranslation( final double tx, final double ty, final double tz )
	{
		final AffineTransform3D translation = new AffineTransform3D();

		translation.set( tx, 0, 3 );
		translation.set( ty, 1, 3 );
		translation.set( tz, 2, 3 );

		return translation;
	}

	public static AbstractTranslation getInitialTranslation( final ViewRegistration vr, final boolean is2d, final AffineTransform3D dsCorrectionT )
	{
		// TODO: somehow fix the contract that the first transformation in the list is a translation
		
		// this one should be the translation
		ViewTransform vt = vr.getTransformList().get( vr.getTransformList().size() - 1 );

		if ( !vt.hasName() || vt.getName().compareTo( "Translation" ) != 0 )
		{
			vt = new ViewTransformAffine( "Translation", createTranslation( 0, 0, 0 ) );
			vr.concatenateTransform( vt );
		}

		final AffineGet affine = vt.asAffine3D();
		
		final double[] target = new double[]{ affine.get( 0, 3 ), affine.get( 1, 3 ), affine.get( 2, 3 ) };
		
		// we go from big to downsampled, thats why the inverse
		dsCorrectionT.applyInverse( target, target );
		
		
		if ( is2d )
			return new Translation2D( target[ 0 ], target[ 1 ] );
		else
			return new Translation3D( target[ 0 ], target[ 1 ], target[ 2 ] );
	}

	public static FinalRealInterval applyTranslation(RealInterval img, AbstractTranslation translation, boolean[] ignoreDims){
		int n = 0;
		for (int d = 0; d < ignoreDims.length; ++d)
			if (!ignoreDims[d])
				n++;
		
		final double [] min = new double [n];
		final double [] max = new double [n];
		
		int i2 = 0;
		for (int i = 0; i< img.numDimensions();++i)
		{
			if (!ignoreDims[i])
			{
				min[i2] = img.realMin(i) + translation.getTranslation(i);
				max[i2] = img.realMax(i) + translation.getTranslation(i);
				i2++;
			}
		}
		return new FinalRealInterval(min, max);
	}
	
	/**
	 * get overlap in local image coordinates (assuming min = (0,0,..))
	 * @param img
	 * @param overlap
	 * @return
	 */
	public static FinalRealInterval getLocalOverlap(RealInterval img, RealInterval overlap){
		final int n = img.numDimensions();
		final double [] min = new double [n];
		final double [] max = new double [n];
		
		for (int i = 0; i< n; i++)
		{
			min[i] = Math.max(0, overlap.realMin(i) - img.realMin(i)) ;
			max[i] = Math.max(0, overlap.realMax(i) - img.realMin(i));
		}
		return new FinalRealInterval(min, max);
	}
	
	/**
	 * create an integer interval from real interval, being conservatie on the size
	 * (min is ceiled, max is floored)
	 * @param overlap
	 * @return
	 */
	public static FinalInterval getLocalRasterOverlap(RealInterval overlap)
	{
		final int n = overlap.numDimensions();
		final long [] min = new long [n];
		final long [] max = new long [n];
		
		for (int i = 0; i< n; i++)
		{
			min[i] = Math.round(Math.ceil(overlap.realMin(i)));
			max[i] = Math.round(Math.floor(overlap.realMax(i)));			
		}
		
		return new FinalInterval(min, max);
	}
	
	
	public static FinalRealInterval getOverlap(final RealInterval img1, final RealInterval img2){
		final int n = img1.numDimensions();
		final double [] min = new double [n];
		final double [] max = new double [n];
		
		for (int i = 0; i< n; i++)
		{
			min[i] = Math.max(img1.realMin(i), img2.realMin(i));
			max[i] = Math.min(img1.realMax(i), img2.realMax(i));
			
			// intervals do not overlap
			if ( max[i] < min [i])
				return null;
		}
		
		return new FinalRealInterval(min, max);
	}
	
	

}
