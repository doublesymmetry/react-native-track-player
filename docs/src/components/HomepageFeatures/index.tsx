import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Lightweight & Feels Native',
    Svg: require('@site/static/img/undraw_audio_player.svg').default,
    description: (
      <>
        Optimized to use the least amount of resources according to your needs.
         As everything is built together, it follows the same design principles as real music apps do.
      </>
    ),
  },
  {
    title: 'Robust Building Blocks',
    Svg: require('@site/static/img/undraw_building_blocks.svg').default,
    description: (
      <>
        Local or network, files or stream. Adaptive bitrate with DASH, HLS or SmoothStreaming.
        Background playback. Caching. Media Controls, and more!
      </>
    ),
  },
  {
    title: 'Multi-Platform Support',
    Svg: require('@site/static/img/undraw_devices.svg').default,
    description: (
      <>
        Supports Android, iOS and Web.
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
