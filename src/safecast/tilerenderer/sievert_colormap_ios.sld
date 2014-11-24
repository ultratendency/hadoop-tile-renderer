<?xml version="1.0" encoding="ISO-8859-1"?>
     <StyledLayerDescriptor version="1.0.0"
      xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"
      xmlns="http://www.opengis.net/sld"
      xmlns:ogc="http://www.opengis.net/ogc"
      xmlns:xlink="http://www.w3.org/1999/xlink"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
       <NamedLayer>
         <Name>Barnes surface</Name>
         <UserStyle>
           <Title>Barnes Surface</Title>
           <Abstract>A style that produces a Barnes surface using a rendering transformation</Abstract>
           <FeatureTypeStyle>
             <Rule>
               <RasterSymbolizer>
                 <!-- specify geometry attribute of input to pass validation -->
                 <Geometry><ogc:PropertyName>geom</ogc:PropertyName></Geometry>
                 <Opacity>1.0</Opacity>
	             	<ColorMap type="ramp" >
	             	   <ColorMapEntry color="#FFFFFF"  quantity="0.00" opacity="0"/>
	                   <ColorMapEntry color="#586abc"  quantity="0.02" opacity="0.7"/>
	                   <ColorMapEntry color="#653ac8"  quantity="0.12" opacity="0.7"/>
	                   <ColorMapEntry color="#e23cfe"  quantity="0.22" opacity="0.7"/>
	                   <ColorMapEntry color="#f72d90"  quantity="0.32" opacity="0.7"/>
	                   <ColorMapEntry color="#d6121c"  quantity="0.42" opacity="0.7"/>
	                   <ColorMapEntry color="#fb3800"  quantity="0.52" opacity="0.7"/>
	                   <ColorMapEntry color="#fb770b"  quantity="0.62" opacity="0.7"/>
	                   <ColorMapEntry color="#fca001"  quantity="0.72" opacity="0.7"/>
	                   <ColorMapEntry color="#f3ca00"  quantity="0.82" opacity="0.7"/>
	                   <ColorMapEntry color="#fff70e"  quantity="0.92" opacity="0.7"/>
	 				</ColorMap>
               </RasterSymbolizer>
              </Rule>
           </FeatureTypeStyle>
         </UserStyle>
       </NamedLayer>
     </StyledLayerDescriptor>
