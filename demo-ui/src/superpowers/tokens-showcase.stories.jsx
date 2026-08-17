/**
 * Tokens Showcase
 *
 * Visual documentation of all available design tokens.
 * Developers can browse this story to understand available values
 * and copy-paste token references into components.
 *
 * Location: Storybook > Tokens > Showcase
 */

import { tokens } from '../foundations/theme'

export default {
  title: 'Tokens/Showcase',
  parameters: {
    layout: 'centered',
    docs: {
      description: {
        component: 'Visual reference for all design tokens. Use token names in components via `import { tokens } from "../foundations/theme"`',
      },
    },
  },
}

/**
 * Spacing Scale
 * Base unit: 4px, multiplied
 */
export const Spacing = () => (
  <div style={{ padding: '20px' }}>
    <h2 style={{ marginBottom: '10px' }}>Spacing Scale</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Base unit: 4px. Usage: <code style={{ backgroundColor: '#eee', padding: '2px 4px', borderRadius: '4px' }}>padding: tokens.spacing[3]</code> → 12px
    </p>
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px' }}>
      {Object.entries(tokens.spacing).map(([key, value]) => (
        <div key={key} style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div
            style={{
              width: value,
              height: value,
              backgroundColor: '#3b82f6',
              borderRadius: '4px',
            }}
          />
          <span style={{ fontSize: '12px', fontWeight: '600' }}>
            tokens.spacing[{key}]
          </span>
          <span style={{ fontSize: '12px', color: '#666' }}>{value}</span>
        </div>
      ))}
    </div>
  </div>
)

/**
 * Typography: Font Sizes
 */
export const FontSizes = () => (
  <div style={{ padding: '20px' }}>
    <h2 style={{ marginBottom: '10px' }}>Font Sizes</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Rem-based for accessibility. Usage: <code style={{ backgroundColor: '#eee', padding: '2px 4px', borderRadius: '4px' }}>fontSize: tokens.fontSize.sm</code>
    </p>
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {Object.entries(tokens.fontSize).map(([key, value]) => (
        <div key={key} style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <span
            style={{
              minWidth: '100px',
              fontSize: '12px',
              fontWeight: '600',
              color: '#333',
            }}
          >
            tokens.fontSize.{key}
          </span>
          <span style={{ fontSize: '12px', color: '#999', minWidth: '50px' }}>
            {value}
          </span>
          <span style={{ fontSize: value }}>
            Quick brown fox jumps
          </span>
        </div>
      ))}
    </div>
  </div>
)

/**
 * Border Radius
 */
export const BorderRadius = () => (
  <div style={{ padding: '20px' }}>
    <h2 style={{ marginBottom: '10px' }}>Border Radius</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Usage: <code style={{ backgroundColor: '#eee', padding: '2px 4px', borderRadius: '4px' }}>borderRadius: tokens.radius.md</code>
    </p>
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px' }}>
      {Object.entries(tokens.radius).map(([key, value]) => (
        <div key={key} style={{ display: 'flex', flexDirection: 'column', gap: '8px', alignItems: 'center' }}>
          <div
            style={{
              width: '100px',
              height: '100px',
              backgroundColor: '#10b981',
              borderRadius: value,
            }}
          />
          <span style={{ fontSize: '12px', fontWeight: '600' }}>
            tokens.radius.{key}
          </span>
          <span style={{ fontSize: '12px', color: '#666' }}>{value}</span>
        </div>
      ))}
    </div>
  </div>
)

/**
 * Shadows: Elevation Levels
 */
export const Shadows = () => (
  <div style={{ padding: '20px' }}>
    <h2 style={{ marginBottom: '10px' }}>Shadows</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Usage: <code style={{ backgroundColor: '#eee', padding: '2px 4px', borderRadius: '4px' }}>boxShadow: tokens.shadow.md</code>
    </p>
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '40px' }}>
      {Object.entries(tokens.shadow).map(([key, value]) => (
        <div key={key} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div
            style={{
              width: '120px',
              height: '80px',
              backgroundColor: '#fff',
              borderRadius: tokens.radius.md,
              boxShadow: value,
            }}
          />
          <span style={{ fontSize: '12px', fontWeight: '600' }}>
            tokens.shadow.{key}
          </span>
          <code style={{ fontSize: '10px', wordBreak: 'break-word', color: '#666' }}>
            {value}
          </code>
        </div>
      ))}
    </div>
  </div>
)

/**
 * Line Height
 */
export const LineHeights = () => (
  <div style={{ padding: '20px' }}>
    <h2 style={{ marginBottom: '10px' }}>Line Height</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Usage: <code style={{ backgroundColor: '#eee', padding: '2px 4px', borderRadius: '4px' }}>lineHeight: tokens.lineHeight.normal</code>
    </p>
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {Object.entries(tokens.lineHeight).map(([key, value]) => (
        <div key={key} style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <span style={{ fontSize: '12px', fontWeight: '600' }}>
            tokens.lineHeight.{key} = {value}
          </span>
          <div style={{ lineHeight: value, fontSize: '16px', maxWidth: '400px' }}>
            The quick brown fox jumps over the lazy dog. This text demonstrates different line heights
            for readability comparison. Tight is 1.25, normal is 1.5, relaxed is 1.75.
          </div>
        </div>
      ))}
    </div>
  </div>
)

/**
 * Font Weight
 */
export const FontWeights = () => (
  <div style={{ padding: '20px' }}>
    <h2 style={{ marginBottom: '10px' }}>Font Weight</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Usage: <code style={{ backgroundColor: '#eee', padding: '2px 4px', borderRadius: '4px' }}>fontWeight: tokens.fontWeight.semibold</code>
    </p>
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {Object.entries(tokens.fontWeight).map(([key, value]) => (
        <div key={key} style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <span style={{ minWidth: '120px', fontSize: '12px', fontWeight: '600' }}>
            tokens.fontWeight.{key}
          </span>
          <span style={{ fontSize: '12px', color: '#999', minWidth: '50px' }}>
            {value}
          </span>
          <span style={{ fontSize: '16px', fontWeight: value }}>
            The quick brown fox
          </span>
        </div>
      ))}
    </div>
  </div>
)

/**
 * Z-Index Scale
 */
export const ZIndex = () => (
  <div style={{ padding: '20px' }}>
    <h2 style={{ marginBottom: '10px' }}>Z-Index Scale</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Usage: <code style={{ backgroundColor: '#eee', padding: '2px 4px', borderRadius: '4px' }}>zIndex: tokens.zIndex.modal</code>
    </p>
    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
      <thead>
        <tr style={{ borderBottom: '2px solid #e5e7eb' }}>
          <th style={{ textAlign: 'left', padding: '12px', fontWeight: '600' }}>
            Level
          </th>
          <th style={{ textAlign: 'left', padding: '12px', fontWeight: '600' }}>
            Token
          </th>
          <th style={{ textAlign: 'left', padding: '12px', fontWeight: '600' }}>
            Value
          </th>
          <th style={{ textAlign: 'left', padding: '12px', fontWeight: '600' }}>
            Use Case
          </th>
        </tr>
      </thead>
      <tbody>
        <tr style={{ borderBottom: '1px solid #e5e7eb' }}>
          <td style={{ padding: '12px' }}>Base</td>
          <td style={{ padding: '12px' }}>tokens.zIndex.base</td>
          <td style={{ padding: '12px' }}>{tokens.zIndex.base}</td>
          <td style={{ padding: '12px' }}>Default element stacking</td>
        </tr>
        <tr style={{ borderBottom: '1px solid #e5e7eb' }}>
          <td style={{ padding: '12px' }}>Dropdown</td>
          <td style={{ padding: '12px' }}>tokens.zIndex.dropdown</td>
          <td style={{ padding: '12px' }}>{tokens.zIndex.dropdown}</td>
          <td style={{ padding: '12px' }}>Dropdown menus</td>
        </tr>
        <tr style={{ borderBottom: '1px solid #e5e7eb' }}>
          <td style={{ padding: '12px' }}>Sticky</td>
          <td style={{ padding: '12px' }}>tokens.zIndex.sticky</td>
          <td style={{ padding: '12px' }}>{tokens.zIndex.sticky}</td>
          <td style={{ padding: '12px' }}>Sticky headers, navigation</td>
        </tr>
        <tr style={{ borderBottom: '1px solid #e5e7eb' }}>
          <td style={{ padding: '12px' }}>Fixed</td>
          <td style={{ padding: '12px' }}>tokens.zIndex.fixed</td>
          <td style={{ padding: '12px' }}>{tokens.zIndex.fixed}</td>
          <td style={{ padding: '12px' }}>Fixed positioning</td>
        </tr>
        <tr style={{ borderBottom: '1px solid #e5e7eb' }}>
          <td style={{ padding: '12px' }}>Modal</td>
          <td style={{ padding: '12px' }}>tokens.zIndex.modal</td>
          <td style={{ padding: '12px' }}>{tokens.zIndex.modal}</td>
          <td style={{ padding: '12px' }}>Modals, overlays</td>
        </tr>
        <tr>
          <td style={{ padding: '12px' }}>Tooltip</td>
          <td style={{ padding: '12px' }}>tokens.zIndex.tooltip</td>
          <td style={{ padding: '12px' }}>{tokens.zIndex.tooltip}</td>
          <td style={{ padding: '12px' }}>Tooltips, popovers (topmost)</td>
        </tr>
      </tbody>
    </table>
  </div>
)
