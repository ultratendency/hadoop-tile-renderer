<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>sievert points</Name>
    <UserStyle>
		<FeatureTypeStyle>  
		      <Rule>
		       <ogc:Filter>
		         <ogc:And>
		           <ogc:PropertyIsGreaterThanOrEqualTo>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.02</ogc:Literal>
		           </ogc:PropertyIsGreaterThanOrEqualTo>
		           <ogc:PropertyIsLessThan>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.12</ogc:Literal>
		           </ogc:PropertyIsLessThan>
		         </ogc:And>
		       </ogc:Filter>
		       <PointSymbolizer>
		         <Graphic>
		           <Mark>
		             <WellKnownName>square</WellKnownName>
		             <Fill>
		               <CssParameter name="fill">#586abc</CssParameter>
		               <CssParameter name="fill-opacity">0.5</CssParameter> 
		             </Fill>
		           </Mark>
		         <Size>8</Size>
		         </Graphic>
		       </PointSymbolizer>
		     </Rule>
		          
		     <Rule>
		       <ogc:Filter>
		         <ogc:And>
		           <ogc:PropertyIsGreaterThanOrEqualTo>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.12</ogc:Literal>
		           </ogc:PropertyIsGreaterThanOrEqualTo>
		           <ogc:PropertyIsLessThan>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.22</ogc:Literal>
		           </ogc:PropertyIsLessThan>
		         </ogc:And>
		       </ogc:Filter>
		       <PointSymbolizer>
		         <Graphic>
		           <Mark>
		             <WellKnownName>square</WellKnownName>
		             <Fill>
		               <CssParameter name="fill">#653ac8</CssParameter>
		               <CssParameter name="fill-opacity">0.5</CssParameter>
		             </Fill>
		           </Mark>
		               <Size>8</Size>
		         </Graphic>
		       </PointSymbolizer>
		     </Rule>
		  
		     <Rule>
		       <ogc:Filter>
		         <ogc:And>
		           <ogc:PropertyIsGreaterThanOrEqualTo>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.22</ogc:Literal>
		           </ogc:PropertyIsGreaterThanOrEqualTo>
		           <ogc:PropertyIsLessThan>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.32</ogc:Literal>
		           </ogc:PropertyIsLessThan>
		         </ogc:And>
		       </ogc:Filter>
		       <PointSymbolizer>
		         <Graphic>
		           <Mark>
		             <WellKnownName>square</WellKnownName>
		             <Fill>
		               <CssParameter name="fill">#e23cfe</CssParameter>
		               <CssParameter name="fill-opacity">0.5</CssParameter>
		             </Fill>
		           </Mark>
		               <Size>8</Size>
		         </Graphic>
		       </PointSymbolizer>
		     </Rule>
		  
		     <Rule>
		       <ogc:Filter>
		         <ogc:And>
		           <ogc:PropertyIsGreaterThanOrEqualTo>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.32</ogc:Literal>
		           </ogc:PropertyIsGreaterThanOrEqualTo>
		           <ogc:PropertyIsLessThan>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.42</ogc:Literal>
		           </ogc:PropertyIsLessThan>
		         </ogc:And>
		       </ogc:Filter>
		       <PointSymbolizer>
		         <Graphic>
		           <Mark>
		             <WellKnownName>square</WellKnownName>
		             <Fill>
		               <CssParameter name="fill">#f72d90</CssParameter>
		               <CssParameter name="fill-opacity">0.5</CssParameter>
		             </Fill>
		           </Mark>
		               <Size>8</Size>
		         </Graphic>
		       </PointSymbolizer>
		     </Rule>
		  
		     <Rule>
		       <ogc:Filter>
		         <ogc:And>
		           <ogc:PropertyIsGreaterThanOrEqualTo>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.42</ogc:Literal>
		           </ogc:PropertyIsGreaterThanOrEqualTo>
		           <ogc:PropertyIsLessThan>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.52</ogc:Literal>
		           </ogc:PropertyIsLessThan>
		         </ogc:And>
		       </ogc:Filter>
		       <PointSymbolizer>
		         <Graphic>
		           <Mark>
		             <WellKnownName>square</WellKnownName>
		             <Fill>
		               <CssParameter name="fill">#d6121c</CssParameter>
		               <CssParameter name="fill-opacity">0.5</CssParameter>
		             </Fill>
		           </Mark>
		               <Size>8</Size>
		         </Graphic>
		       </PointSymbolizer>
		     </Rule>
		  
		    
		      <Rule>
		       <ogc:Filter>
		         <ogc:And>
		           <ogc:PropertyIsGreaterThanOrEqualTo>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.52</ogc:Literal>
		           </ogc:PropertyIsGreaterThanOrEqualTo>
		           <ogc:PropertyIsLessThan>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.62</ogc:Literal>
		           </ogc:PropertyIsLessThan>
		         </ogc:And>
		       </ogc:Filter>
		       <PointSymbolizer>
		         <Graphic>
		           <Mark>
		             <WellKnownName>square</WellKnownName>
		             <Fill>
		               <CssParameter name="fill">#fb3800</CssParameter>
		               <CssParameter name="fill-opacity">0.5</CssParameter>
		             </Fill>
		           </Mark>
		               <Size>8</Size>
		         </Graphic>
		       </PointSymbolizer>
		     </Rule>
		  
		  
		     <Rule>
		       <ogc:Filter>
		         <ogc:And>
		           <ogc:PropertyIsGreaterThanOrEqualTo>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.62</ogc:Literal>
		           </ogc:PropertyIsGreaterThanOrEqualTo>
		           <ogc:PropertyIsLessThan>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.72</ogc:Literal>
		           </ogc:PropertyIsLessThan>
		         </ogc:And>
		       </ogc:Filter>
		       <PointSymbolizer>
		         <Graphic>
		           <Mark>
		             <WellKnownName>square</WellKnownName>
		             <Fill>
		               <CssParameter name="fill">#fb770b</CssParameter>
		               <CssParameter name="fill-opacity">0.5</CssParameter>
		             </Fill>
		           </Mark>
		               <Size>8</Size>
		         </Graphic>
		       </PointSymbolizer>
		     </Rule>
		  
		     <Rule>
		       <ogc:Filter>
		         <ogc:And>
		           <ogc:PropertyIsGreaterThanOrEqualTo>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.72</ogc:Literal>
		           </ogc:PropertyIsGreaterThanOrEqualTo>
		           <ogc:PropertyIsLessThan>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.82</ogc:Literal>
		           </ogc:PropertyIsLessThan>
		         </ogc:And>
		       </ogc:Filter>
		       <PointSymbolizer>
		         <Graphic>
		           <Mark>
		             <WellKnownName>square</WellKnownName>
		             <Fill>
		               <CssParameter name="fill">#fca001</CssParameter>
		               <CssParameter name="fill-opacity">0.5</CssParameter>
		             </Fill>
		           </Mark>
		               <Size>8</Size>
		         </Graphic>
		       </PointSymbolizer>
		     </Rule>
		  
		     <Rule>
		       <ogc:Filter>
		         <ogc:And>
		           <ogc:PropertyIsGreaterThanOrEqualTo>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.82</ogc:Literal>
		           </ogc:PropertyIsGreaterThanOrEqualTo>
		           <ogc:PropertyIsLessThan>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.92</ogc:Literal>
		           </ogc:PropertyIsLessThan>
		         </ogc:And>
		       </ogc:Filter>
		       <PointSymbolizer>
		         <Graphic>
		           <Mark>
		             <WellKnownName>square</WellKnownName>
		             <Fill>
		               <CssParameter name="fill">#f3ca00</CssParameter>
		               <CssParameter name="fill-opacity">0.5</CssParameter>
		             </Fill>
		           </Mark>
		             <Size>8</Size>
		         </Graphic>
		       </PointSymbolizer>
		     </Rule>
		  
		      <Rule>
		       <ogc:Filter>
		         <ogc:And>
		           <ogc:PropertyIsGreaterThanOrEqualTo>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>0.92</ogc:Literal>
		           </ogc:PropertyIsGreaterThanOrEqualTo>
		           <ogc:PropertyIsLessThan>
		             <ogc:PropertyName>value</ogc:PropertyName>
		             <ogc:Literal>1000</ogc:Literal>
		           </ogc:PropertyIsLessThan>
		         </ogc:And>
		       </ogc:Filter>
		       <PointSymbolizer>
		         <Graphic>
		           <Mark>
		             <WellKnownName>square</WellKnownName>
		             <Fill>
		               <CssParameter name="fill">#fff70e</CssParameter>
		               <CssParameter name="fill-opacity">0.5</CssParameter>
		             </Fill>
		           </Mark>
		             <Size>8</Size>
		         </Graphic>
		       </PointSymbolizer>
		     </Rule>
		     	  
		</FeatureTypeStyle> 
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>