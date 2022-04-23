import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import HomepageFeatures from '@site/src/components/HomepageFeatures';

type Badge = { badge: string; link: string, alt: string };
const BADGES: Badge[] = [
  {
    alt: 'downloads',
    badge: 'https://img.shields.io/npm/dw/react-native-track-player.svg',
    link: 'https://www.npmjs.com/package/react-native-track-player',
  },
  {
    alt: 'npm',
    badge: 'https://img.shields.io/npm/v/react-native-track-player.svg',
    link: 'https://www.npmjs.com/package/react-native-track-player',
  },
  {
    alt: 'discord',
    badge: 'https://img.shields.io/discord/567636850513018880.svg',
    link: 'https://discordapp.com/invite/ya2XDCR',
  },
];

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <img
          src="/img/logo.svg"
          style={{ width: 100, height: 100 }}
          className="logo"
        />
        <h1 className="hero__title">{siteConfig.title}</h1>
        <p className="hero__subtitle">{siteConfig.tagline}</p>
        <div>
          {BADGES.map(({ alt, badge, link }, idx) => (
            <Link to={link} key={idx} style={{ padding: 5 }}>
              <img src={badge} alt={alt} />
            </Link>
          ))}
        </div>
        <hr/>
        <div className={styles.buttons}>
          <Link
            style={{ margin: 5 }}
            className="button button--secondary button--lg"
            to="/docs/basics/installation">
            Get Started
          </Link>

          <Link
            style={{ margin: 5 }}
            className="button button--lg"
            to="https://discordapp.com/invite/ya2XDCR">
            Join the Community
          </Link>
        </div>
      </div>
    </header>
  );
}

export default function Home(): JSX.Element {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={siteConfig.title}
      description="A fully fledged audio module created for music apps. Provides audio playback, external media controls, background mode and more!">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
