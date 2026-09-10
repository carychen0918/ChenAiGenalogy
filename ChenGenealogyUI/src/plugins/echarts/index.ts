import * as echarts from 'echarts/core'

import {
  BarChart,
  EffectScatterChart,
  FunnelChart,
  GaugeChart,
  LineChart,
  LinesChart,
  MapChart,
  PictorialBarChart,
  PieChart,
  RadarChart,
  ScatterChart
} from 'echarts/charts'

import {
  AriaComponent,
  DataZoomComponent,
  GeoComponent,
  GridComponent,
  LegendComponent,
  ParallelComponent,
  PolarComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  VisualMapComponent
} from 'echarts/components'

import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  ToolboxComponent,
  DataZoomComponent,
  GridComponent,
  PolarComponent,
  AriaComponent,
  ParallelComponent,
  VisualMapComponent,
  GeoComponent,
  BarChart,
  LineChart,
  LinesChart,
  PieChart,
  MapChart,
  ScatterChart,
  EffectScatterChart,
  CanvasRenderer,
  PictorialBarChart,
  RadarChart,
  GaugeChart,
  FunnelChart
])

export default echarts
